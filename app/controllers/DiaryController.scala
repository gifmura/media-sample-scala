package controllers

import javax.inject.Inject
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class DiaryController @Inject()(repo: DiaryRepository
                                  , cc: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  val diaryForm: Form[CreateDiaryForm] = Form{
    mapping(
      "accountId" -> longNumber,
      "imageId" -> optional(longNumber),
      "title" -> nonEmptyText,
      "body" -> nonEmptyText
    )(CreateDiaryForm.apply)(CreateDiaryForm.unapply)
  }

  def edit = Action  { implicit request =>
    Ok(views.html.edit(diaryForm))
  }

  def archive = Action.async { implicit request =>
    diaryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.edit(errorForm)))
      },
      diary => {
        repo.create(diary.accountId, diary.imageId, diary.title, diary.body).map { _ =>
          Redirect(routes.DiaryController.edit).flashing("success" -> "entry.created")
        }
      }
    )
  }

  def getDiaries = Action.async{ implicit request =>
    repo.list().map{ diaries =>
      Ok(Json.toJson(diaries))
    }
  }
}

case class CreateDiaryForm(accountId:Long, imageId:Option[Long], title:String, body:String)