package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject()(repo: AccountRepository
                                  , cc: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  val accountForm: Form[CreateAccountForm] = Form{
    mapping(
      "name" -> nonEmptyText(1,20),
      "password" -> nonEmptyText(8,20)
    )(CreateAccountForm.apply)(CreateAccountForm.unapply)
  }

  def register = Action { implicit request =>
    Ok(views.html.register(accountForm))
  }

  def addAccount = Action.async{ implicit request =>
    accountForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(Ok(views.html.register(errorForm)))
      },
      account =>{
        repo.create(account.name, account.password).map{_ =>
          Redirect(routes.LandingPageController.showLandingPage())
            .flashing("success" -> "account.created")
        }
      }
    )
  }

  // For test.
  def getAccounts = Action.async{ implicit request =>
    repo.list().map{ accounts =>
      Ok(Json.toJson(accounts))
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.login(accountForm))
  }

  def attempt = Action.async{ implicit request =>
    accountForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(Ok(views.html.login(errorForm)))
      },
      account =>{
        repo.isExist(account.name, account.password).map{isExist =>
          if (isExist == true) {
            Redirect(routes.LandingPageController.showLandingPage)
              .flashing("success" -> "You are logged in.")
              .withSession(Global.SESSION_ACCOUNTNAME_KEY -> account.name)
          } else {
            Redirect(routes.AccountController.login)
              .flashing("error" -> "Invalid username/password.")
          }
        }
      }
    )
  }
}

case class CreateAccountForm(name:String, password:String)