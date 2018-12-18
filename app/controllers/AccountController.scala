package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject()(
    repo: AccountRepository,
    cc: MessagesControllerComponents,
    authenticatedUserAction: AuthenticatedAccountAction)(
    implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  val accountForm: Form[CreateAccountForm] = Form {
    mapping(
      "name" -> nonEmptyText(1, 20),
      "password" -> nonEmptyText(8, 20)
    )(CreateAccountForm.apply)(CreateAccountForm.unapply)
  }

  def register = Action { implicit request =>
    Ok(views.html.register(accountForm))
  }

  def postAccount: Action[AnyContent] = Action.async { implicit request =>
    accountForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.register(errorForm)))
      },
      account => {
        repo.create(account.name, account.password).map { _ =>
          Redirect(routes.LandingPageController.showLandingPage())
            .flashing("success" -> "account.created")
        }
      }
    )
  }

  def login = Action { implicit request =>
    Ok(views.html.login(accountForm))
  }

  def attempt: Action[AnyContent] = Action.async { implicit request =>
    accountForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.login(errorForm)))
      },
      account => {
        (repo getId (account.name, account.password)).map {
          case None =>
            Redirect(routes.AccountController.login ())
              .flashing("error" -> "Invalid name/password.")
          case Some(id) =>
            Redirect(routes.LandingPageController.showLandingPage ())
              .flashing("success" -> "You are logged in.")
              .withSession(Constant.SESSION_ACCOUNTID_KEY -> id.toString)
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

case class CreateAccountForm(name: String, password: String)
