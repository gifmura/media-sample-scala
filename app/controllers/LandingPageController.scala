package controllers

import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class LandingPageController @Inject()(cc: MessagesControllerComponents)(
    implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  def showLandingPage = Action { implicit request =>
    Ok(views.html.landing(request))
  }
}
