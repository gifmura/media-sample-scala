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
      "name" -> nonEmptyText
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
        repo.create(account.name).map{_ =>
          Redirect(routes.AccountController.index).flashing("success" -> "account.created")
        }
      }
    )
  }

  def getAccounts = Action.async{ implicit request =>
    repo.list().map{ accounts =>
      Ok(Json.toJson(accounts))
    }
  }
}

case class CreateAccountForm(name: String)