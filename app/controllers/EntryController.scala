package controllers

import java.io.File
import java.nio.file.{Files, Path, Paths}

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import javax.inject.Inject
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import play.core.parsers.Multipart.FileInfo

import scala.concurrent.{ExecutionContext, Future}

class EntryController @Inject()(repo: EntryRepository
                                  , cc: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

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

  def edit = Action { implicit request =>
    Ok(views.html.edit(entryForm))
  }

  def archive = Action(parse.multipartFormData(handleFilePartAsFile)).async { implicit request =>
    entryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.edit(errorForm)))
      },
      entry => {
        logger.info("insert into entry.")
        val fileUrl = request.body.file("img").map{
          case FilePart(key, filename, contentType, file) =>
            logger.info(s"key = ${key}, filename = ${filename}, contentType = ${contentType}, file = $file")
            val path = operateOnTempFile(file)
            path.toString
        }
        repo.create(entry.accountId, fileUrl, entry.title, entry.body).map { _ =>
          Redirect(routes.LandingPageController.showLandingPage()).flashing("success" -> "entry.created")
        }
      }
    )
  }

  // For test.
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

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val path: Path = Files.createTempFile("img", "")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          logger.info(s"count = $count, status = $status")
          FilePart(partName, filename, contentType, path.toFile)
      }
  }

  private def operateOnTempFile(tmpFile: File) = {
    val src = tmpFile.toPath
    val dest = Paths.get("/","tmp", "mediasample", src.getFileName.toString)
    logger.info(s"dest = ${dest}")
    val newFile = Files.copy(src, dest)
    Files.deleteIfExists(src)
    newFile
  }
}

case class CreateEntryForm(accountId:Long, imageUrl:Option[String], title:String, body:String)