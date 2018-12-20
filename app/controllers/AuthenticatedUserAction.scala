package controllers

import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedUserAction @Inject()(parser: BodyParsers.Default)(
    implicit ec: ExecutionContext)
    extends ActionBuilderImpl(parser) {

  private val logger = play.api.Logger(this.getClass)

  override def invokeBlock[A](
      request: Request[A],
      block: Request[A] => Future[Result]): Future[Result] = {
    logger.info("ENTERED AuthenticatedUserAction::invokeBlock ...")
    val maybeUserId =
      request.session.get(models.Constant.SESSION_USER_KEY)
    maybeUserId match {
      case None =>
        Future.successful(Forbidden("Dude, you’re not logged in."))
      case Some(u) =>
        val res: Future[Result] = block(request)
        res
    }
  }
}
