import controllers.{AuthenticatedUserAction, UserController}
import models.{Constant, User, UserRepository}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserControllerSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory
    with Results {

  val cc = stubMessagesControllerComponents()
  val mockedAuthUserAction: AuthenticatedUserAction =
    mock[AuthenticatedUserAction]

  def stubMessagesControllerComponents(): MessagesControllerComponents = {
    val stub = Helpers.stubControllerComponents()
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(
        stubBodyParser(AnyContentAsEmpty),
        stub.messagesApi)(stub.executionContext),
      DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
      stub.parsers,
      stub.messagesApi,
      stub.langs,
      stub.fileMimeTypes,
      stub.executionContext
    )
  }

  "UserController#register" should {
    "should be OK" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val mockedUserRepository: UserRepository = mock[UserRepository]
      val controller =
        new UserController(mockedUserRepository, cc, mockedAuthUserAction)

      val request =
        FakeRequest().withCSRFToken
      val result = controller.register.apply(request)

      status(result) mustBe (OK)
      contentType(result) mustBe Some("text/html")
    }
  }

  "UserController#postUser" should {
    "should be SEE_OTHER" in {
      val userId: Long = 1
      val email = "sample-address@media-sample-scala.com"
      val password = "password"
      val name = "unit-test"
      val mockedUserRepository: UserRepository = mock[UserRepository]
      when(mockedUserRepository.create(email, password, name)) thenReturn Future {
        new User(userId, email, password, name)
      }

      val controller =
        new UserController(mockedUserRepository, cc, mockedAuthUserAction)
      val request =
        FakeRequest("POST", "/postUser").withJsonBody(Json.parse(s"""{
                                                                    |  "email": "$email",
                                                                    |  "password": "$password",
                                                                    |  "name": "$name"
                                                                    |}""".stripMargin))

      val result = controller.postUser.apply(request)
      val expectedFlash = Flash(Map("success" -> "user.created"))

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
    }
  }

  "UserController#login" should {
    "should be OK" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val mockedUserRepository: UserRepository = mock[UserRepository]
      val controller =
        new UserController(mockedUserRepository, cc, mockedAuthUserAction)

      val request =
        FakeRequest().withCSRFToken
      val result = controller.login.apply(request)

      status(result) mustBe (OK)
      contentType(result) mustBe Some("text/html")
    }
  }

  "UserController#attempt" should {
    "should be SEE_OTHER with new session if the user has already registered" in {
      val userId: Long = 1
      val email = "sample-address@media-sample-scala.com"
      val password = "password"
      val mockedUserRepository: UserRepository = mock[UserRepository]
      when(mockedUserRepository.getId(email, password)) thenReturn Future(
        Option(userId))

      val controller =
        new UserController(mockedUserRepository, cc, mockedAuthUserAction)
      val request =
        FakeRequest().withJsonBody(Json.parse(s"""{
                                              |  "email": "$email",
                                              |  "password": "$password"
                                              |}""".stripMargin))

      val result = controller.attempt.apply(request)
      val expectedFlash = Flash(Map("success" -> "You are logged in."))
      val expectedSession =
        Session(Map(Constant.SESSION_USER_KEY -> userId.toString))

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
      session(result) must equal(expectedSession)
    }
  }

  "UserController#attempt" must {
    "must not start new session if the user has not registered" in {
      val email = "dummy-address@media-sample-scala.com"
      val password = "dummy-password"
      val mockedUserRepository: UserRepository = mock[UserRepository]
      when(mockedUserRepository.getId(email, password)) thenReturn Future(None)

      val controller =
        new UserController(mockedUserRepository, cc, mockedAuthUserAction)
      val request =
        FakeRequest().withJsonBody(Json.parse(s"""{
                                                 |  "email": "$email",
                                                 |  "password": "$password"
                                                 |}""".stripMargin))

      val result = controller.attempt.apply(request)
      val expectedFlash = Flash(Map("error" -> "Invalid name/password."))
      val expectedSession = Session(Map())

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
      session(result) must equal(expectedSession)
    }
  }

//  "UserController#logout" should {
//    "should refresh the session" in {
//      val mockedUserRepository: UserRepository = mock[UserRepository]
//
//      val userId = 1
//      val request =
//        FakeRequest()
//          .withSession((Constant.SESSION_USER_KEY, userId.toString))
//      val authUserAction = new AuthenticatedUserAction(parser)
//
//      val mockedAuthUserAction: AuthenticatedUserAction =
//        mock[AuthenticatedUserAction]
//      when(mockedAuthUserAction.invokeBlock(Matchers.any(), Matchers.any()))
//        .thenReturn(Future(Result(ResponseHeader(200), HttpEntity.NoEntity)))
//
//      val controller =
//        new UserController(mockedUserRepository, cc, authUserAction)
//
//      val result = controller.logout.apply(request)
//      val expectedFlash = Flash(Map("info" -> "You are logged out."))
//      val expectedSession = Session(Map())
//
//      status(result) must equal(SEE_OTHER)
//      flash(result) must equal(expectedFlash)
//      session(result) must equal(expectedSession)
//    }
//  }

}
