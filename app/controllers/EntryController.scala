package controllers

import java.io.File
import java.nio.file.{Files, Path}

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import javax.inject.Inject
import jp.t2v.lab.play2.pager.Pager
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import services.EntryService

import scala.concurrent.{ExecutionContext, Future}

class EntryController @Inject()(
    repo: EntryRepository,
    service: EntryService,
    cc: MessagesControllerComponents,
    userAction: UserInfoAction
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  val entryForm: Form[CreateEntryForm] = Form {
    mapping(
      "imageUri" -> optional(text),
      "title" -> nonEmptyText(1, 100),
      "content" -> nonEmptyText
    )(CreateEntryForm.apply)(CreateEntryForm.unapply)
  }

  def index(pager: Pager[Entry]): Action[AnyContent] = Action.async {
    implicit request =>
      repo.getEntries.map { _ =>
        Redirect(routes.EntryController.list(pager))
      }
  }

  def edit: Action[AnyContent] = userAction { implicit request =>
    logger.info(s"$SESSION_ID = ${request.session.get(SESSION_ID)}")
    Ok(views.html.edit(entryForm))
  }

  def archive: Action[MultipartFormData[File]] =
    userAction(parse.multipartFormData(handleFilePartAsFile))
      .async { implicit request =>
        entryForm.bindFromRequest.fold(
          errorForm => {
            Future.successful(Ok(views.html.edit(errorForm)))
          },
          entry => {
            val userId = request.userInfo.get.userId.toLong
            service
              .createEntry(userId,
                           entry.title,
                           entry.content,
                           request.body.file("img"))
              .map { _ =>
                Redirect(routes.LandingPageController.showLandingPage())
                  .flashing(FLASH_SUCCESS -> "entry.created")
              }
          }
        )
      }

  def list(pager: Pager[Entry]): Action[AnyContent] = Action.async {
    implicit request =>
      service.findAll(pager).map { p =>
        Ok(views.html.list(p))
      }
  }

  def entry(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.getEntry(id).map {
      case Some(p) =>
        Ok(views.html.entry(p))
      case None =>
        Forbidden("This page does not exist.")
    }
  }

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val path: Path = Files.createTempFile("img", ".png")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          logger.info(s"count = $count, status = $status")
          FilePart(partName, filename, contentType, path.toFile)
      }
  }
}

case class CreateEntryForm(imageUri: Option[String],
                           title: String,
                           content: String)
