import javax.inject.{Inject, Singleton}
import jp.t2v.lab.play2.pager.Pager
import models.Entry
import play.api.http.SecretConfiguration
import play.api.i18n.MessagesApi
import play.api.libs.json.{Format, Json}
import play.api.mvc._
import services.encryption.{EncryptedCookieBaker, EncryptionService}
import services.session.SessionService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Methods and objects common to all controllers
  */
package object controllers {

  import play.api.data.Form
  import play.api.data.Forms._

  val SESSION_ID = "sessionId"

  val FLASH_SUCCESS = "success"
  val FLASH_ERROR = "error"

  val USER_INFO_COOKIE_NAME = "userInfo"

  case class UserInfo(userId: String)

  object UserInfo {
    implicit val format: Format[UserInfo] = Json.format[UserInfo]
  }

  val form = Form(
    mapping(
      "user_id" -> text
    )(UserInfo.apply)(UserInfo.unapply)
  )

  def discardingSession(result: Result): Result = {
    result.withNewSession.discardingCookies(
      DiscardingCookie(USER_INFO_COOKIE_NAME))
  }

  /**
    * An action that pulls everything together to show user info that is in an encrypted cookie,
    * with only the secret key stored on the server.
    */
  @Singleton
  class UserInfoAction @Inject()(
      sessionService: SessionService,
      factory: UserInfoCookieBakerFactory,
      playBodyParsers: PlayBodyParsers,
      messagesApi: MessagesApi
  )(implicit val executionContext: ExecutionContext)
      extends ActionBuilder[UserRequest, AnyContent]
      with Results {

    override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

    override def invokeBlock[A](
        request: Request[A],
        block: UserRequest[A] => Future[Result]): Future[Result] = {
      val maybeFutureResult: Option[Future[Result]] = for {
        sessionId <- request.session.get(SESSION_ID)
        userInfoCookie <- request.cookies.get(USER_INFO_COOKIE_NAME)
      } yield {
        sessionService.lookup(sessionId).flatMap {
          case Some(secretKey) =>
            val cookieBaker = factory.createCookieBaker(secretKey)
            val maybeUserInfo =
              cookieBaker.decodeFromCookie(Some(userInfoCookie))

            block(new UserRequest[A](request, maybeUserInfo, messagesApi))
          case None =>
            Future.successful {
              discardingSession {
                Redirect(routes.EntryController.index(Pager.default[Entry]))
              }.flashing(FLASH_ERROR -> "Your session has expired!")
            }
        }
      }

      maybeFutureResult.getOrElse {
        Future.successful(Forbidden("Dude, you’re not logged in."))
      }
    }
  }

  trait UserRequestHeader
      extends PreferredMessagesProvider
      with MessagesRequestHeader {
    def userInfo: Option[UserInfo]
  }

  class UserRequest[A](
      request: Request[A],
      val userInfo: Option[UserInfo],
      val messagesApi: MessagesApi
  ) extends WrappedRequest[A](request)
      with UserRequestHeader

  /**
    * Creates a cookie baker with the given secret key.
    */
  @Singleton
  class UserInfoCookieBakerFactory @Inject()(
      encryptionService: EncryptionService,
      secretConfiguration: SecretConfiguration
  ) {

    def createCookieBaker(
        secretKey: Array[Byte]): EncryptedCookieBaker[UserInfo] = {
      new EncryptedCookieBaker[UserInfo](secretKey,
                                         encryptionService,
                                         secretConfiguration) {
        override val expirationDate: FiniteDuration = 365.days
        override val COOKIE_NAME: String = USER_INFO_COOKIE_NAME
      }
    }
  }

  @Singleton
  class SessionGenerator @Inject()(
      sessionService: SessionService,
      userInfoService: EncryptionService,
      factory: UserInfoCookieBakerFactory
  )(implicit ec: ExecutionContext) {

    def createSession(userInfo: UserInfo): Future[(String, Cookie)] = {
      val secretKey = userInfoService.newSecretKey
      val cookieBaker = factory.createCookieBaker(secretKey)
      val userInfoCookie = cookieBaker.encodeAsCookie(Some(userInfo))

      sessionService
        .create(secretKey)
        .map(sessionId => (sessionId, userInfoCookie))
    }

  }

}
