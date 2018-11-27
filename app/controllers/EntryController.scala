package controllers

import javax.inject.Inject
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class EntryController @Inject()(repo: EntryRepository
                                  , cc: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  val entryForm: Form[CreateEntryForm] = Form{
    mapping(
      "accountId" -> longNumber,
      "imageUrl" -> optional(text),
      "title" -> nonEmptyText,
      "body" -> nonEmptyText
    )(CreateEntryForm.apply)(CreateEntryForm.unapply)
  }

  def index = Action { implicit request =>
    Redirect(routes.EntryController.list())
  }

  def edit = Action  { implicit request =>
    Ok(views.html.edit(entryForm))
  }

  def archive = Action.async { implicit request =>
    entryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.edit(errorForm)))
      },
      entry => {
        repo.create(entry.accountId, entry.imageUrl, entry.title, entry.body).map { _ =>
          Redirect(routes.LandingPageController.showLandingPage()).flashing("success" -> "entry.created")
        }
      }
    )
  }

  def getDiaries = Action.async{ implicit request =>
    repo.list().map{ diaries =>
      Ok(Json.toJson(diaries))
    }
  }

  def list = Action.async{ implicit request =>
    repo.getEntries().map{ p =>
      Ok(views.html.list(p))
    }
  }

  def entry(id:Long) = Action.async{ implicit request =>
    repo.getEntry(id).map{ p =>
      Ok(views.html.entry(p.head))
    }
  }
}

case class CreateEntryForm(accountId:Long, imageUrl:Option[String], title:String, body:String)