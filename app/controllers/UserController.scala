package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services.session.SessionService

import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(
    repo: UserRepository,
    cc: MessagesControllerComponents,
    userAction: UserInfoAction,
    sessionService: SessionService,
    sessionGenerator: SessionGenerator)(implicit ec: ExecutionContext)
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

  def register: Action[AnyContent] = Action { implicit request =>
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
            .flashing(FLASH_SUCCESS -> "user.created")
        }
      }
    )
  }

  def login: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(loginUserForm))
  }

  def attempt: Action[AnyContent] = Action.async { implicit request =>
    loginUserForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.login(errorForm)))
      },
      user => {
        repo
          .getId(user.email, user.password)
          .map {
            case Some(id) =>
              sessionGenerator.createSession(UserInfo(id.toString)).map {
                case (sessionId, encryptedCookie) =>
                  val session = request.session + (SESSION_ID -> sessionId)
                  Redirect(routes.LandingPageController.showLandingPage())
                    .flashing(FLASH_SUCCESS -> "You are logged in.")
                    .withSession(session)
                    .withCookies(encryptedCookie)
              }
            case None =>
              Future(
                Redirect(routes.UserController.login())
                  .flashing(FLASH_ERROR -> "Invalid name/password."))
          }
          .flatten
      }
    )
  }

  def logout: Action[AnyContent] = userAction { implicit request =>
    request.session.get(SESSION_ID).foreach { sessionId =>
      sessionService.delete(sessionId)
    }

    discardingSession {
      Redirect(routes.LandingPageController.showLandingPage())
        .flashing(FLASH_SUCCESS -> "You are logged out.")
    }
  }

}

case class CreateUserForm(email: String, password: String, name: String)
case class LoginUserForm(email: String, password: String)
