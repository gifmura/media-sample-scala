package controllers

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.session.SessionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DummyUserInfoAction @Inject()(
    sessionService: SessionService,
    factory: UserInfoCookieBakerFactory,
    playBodyParsers: PlayBodyParsers,
    messagesApi: MessagesApi
) extends UserInfoAction(sessionService,
                           factory,
                           playBodyParsers,
                           messagesApi) {

  override def invokeBlock[A](
      request: Request[A],
      block: UserRequest[A] => Future[Result]): Future[Result] = {
    block(new UserRequest[A](request, None, messagesApi))
  }
}
