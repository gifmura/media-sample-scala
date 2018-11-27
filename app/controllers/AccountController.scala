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

  def index = Action { implicit request =>
    Ok(views.html.index(accountForm))
  }

  def addAccount = Action.async{ implicit request =>
    accountForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(Ok(views.html.index(errorForm)))
      },
      account =>{
        repo.create(account.name, account.password).map{_ =>
          Redirect(routes.AccountController.index)
            .flashing("success" -> "account.created")
        }
      }
    )
  }

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
//        val trueRes = Await.result(repo.isExist(account.name, account.password), Duration.Inf)
//        if (trueRes) {
//          Redirect(routes.AccountController.login)
//            .flashing("info" -> "You are logged in.")
//            .withSession(Global.SESSION_ACCOUNTNAME_KEY -> account.name)
//        } else {
//          Redirect(routes.AccountController.login)
//            .flashing("error" -> "Invalid username/password.")
//        }
      }
    )
  }

//  def attempt_ = Action { implicit request =>
//    val errorFunction = { formWithErrors: Form[Account] =>
//      // form validation/binding failed...
//      BadRequest(views.html.login(formWithErrors, formSubmitUrl))
//    }
//    val successFunction = { account: Account =>
//      // form validation/binding succeeded ...
//      val trueRes = Await.result(repo.isExist(account.name, account.password), Duration.Inf)
//      if (trueRes) {
//        Redirect(routes.AccountController.login)
//          .flashing("info" -> "You are logged in.")
//          .withSession(Global.SESSION_ACCOUNTNAME_KEY -> account.name)
//      } else {
//        Redirect(routes.AccountController.login)
//          .flashing("error" -> "Invalid username/password.")
//      }
//    }
//    val formValidationResult: Form[Account] = accountForm.bindFromRequest
//    formValidationResult.fold(
//      errorFunction,
//      successFunction
//    )
//  }
}
//val isExist = repo.isExist()

case class CreateAccountForm(name:String, password:String)