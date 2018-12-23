package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(
    repo: UserRepository,
    cc: MessagesControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  val createUserForm: Form[CreateUserForm] = Form {
    mapping(
      "email" -> nonEmptyText(1, 50),
      "password" -> nonEmptyText(8, 20),
      "name" -> nonEmptyText(1, 20)
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }

  val loginUserForm: Form[LoginUserForm] = Form {
    mapping(
      "email" -> nonEmptyText(1, 50),
      "password" -> nonEmptyText(8, 20)
    )(LoginUserForm.apply)(LoginUserForm.unapply)
  }

  def register = Action { implicit request =>
    Ok(views.html.register(createUserForm))
  }

  def postUser: Action[AnyContent] = Action.async { implicit request =>
    createUserForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.register(errorForm)))
      },
      user => {
        repo.create(user.email, user.password, user.name).map { _ =>
          Redirect(routes.LandingPageController.showLandingPage())
            .flashing("success" -> "user.created")
        }
      }
    )
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginUserForm))
  }

  def attempt: Action[AnyContent] = Action.async { implicit request =>
    loginUserForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.login(errorForm)))
      },
      user => {
        (repo getId (user.email, user.password)).map {
          case Some(id) =>
            Redirect(routes.LandingPageController.showLandingPage())
              .flashing("success" -> "You are logged in.")
              .withSession(Constant.SESSION_USER_KEY -> id.toString)
          case None =>
            Redirect(routes.UserController.login())
              .flashing("error" -> "Invalid name/password.")
        }
      }
    )
  }

  def logout = authenticatedUserAction {
      implicit request: Request[AnyContent] =>
      Redirect(routes.LandingPageController.showLandingPage())
        .flashing("info" -> "You are logged out.")
        .withNewSession
  }
}

case class CreateUserForm(email: String, password: String, name: String)
case class LoginUserForm(email: String, password: String)
