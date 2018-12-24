package controllers

import java.io.File
import java.nio.file.{Files, Path}

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import javax.inject.Inject
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import service.EntryService

import scala.concurrent.{ExecutionContext, Future}

class EntryController @Inject()(
    repo: EntryRepository,
    service: EntryService,
    cc: MessagesControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with play.api.i18n.I18nSupport {

  private val logger = Logger(this.getClass)

  val entryForm: Form[CreateEntryForm] = Form {
    mapping(
      "imageUri" -> optional(text),
      "title" -> nonEmptyText(1, 100),
      "content" -> nonEmptyText
    )(CreateEntryForm.apply)(CreateEntryForm.unapply)
  }

  def index: Action[AnyContent] = Action.async { implicit request =>
    repo.getEntries.map { p =>
      Ok(views.html.list(p))
    }
  }

  def edit = authenticatedUserAction { implicit request =>
    logger.info(
      s"UserId = ${request.session.get(Constant.SESSION_USER_KEY).get}")
    Ok(views.html.edit(entryForm))
  }

  def archive: Action[MultipartFormData[File]] =
    authenticatedUserAction(parse.multipartFormData(handleFilePartAsFile))
      .async { implicit request =>
        entryForm.bindFromRequest.fold(
          errorForm => {
            Future.successful(Ok(views.html.edit(errorForm)))
          },
          entry => {
            val imgFile = request.body.file("img").map {
              case FilePart(key, filename, contentType, file) =>
                logger.info(
                  s"key = $key, filename = $filename, contentType = $contentType, file = $file")
                val size = Files.size(file.toPath)
                val path =
                  if (size > 0)
                    copyTempFile(file)
                  else null
                deleteTempFile(file)
                (Option(path), Option(size))
            }
            val userId =
              request.session.get(Constant.SESSION_USER_KEY).get.toLong
            service
              .create(userId,
                      entry.title,
                      entry.content,
                      imgFile.get._1,
                      imgFile.get._2)
              .map { _ =>
                Redirect(routes.LandingPageController.showLandingPage())
                  .flashing("success" -> "entry.created")
              }
          }
        )
      }

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.getEntries.map { p =>
      Ok(views.html.list(p))
    }
  }

  def entry(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.getEntry(id).map {
      case Some(p) =>
        Ok(views.html.entry(p))
      case None =>
        (Forbidden("This page does not exist."))
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

  private def copyTempFile(tmpFile: File) = {
    val src = tmpFile.toPath
    val dest = ImageRepository.IMAGE_DIRECTORY.resolve(src.getFileName.toString)
    logger.info(s"dest = $dest")
    val newFile = Files.copy(src, dest)
    newFile.toString
  }

  private def deleteTempFile(tmpFile: File) = {
    val src = tmpFile.toPath
    Files.deleteIfExists(src)
  }
}

case class CreateEntryForm(imageUri: Option[String],
                           title: String,
                           content: String)
