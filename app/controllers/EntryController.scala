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
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo

import scala.concurrent.{ExecutionContext, Future}

class EntryController @Inject()(
    repo: EntryRepository,
    imgRepo: ImageRepository,
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
            logger.info("insert into entry.")
            val fileUri = request.body.file("img").flatMap {
              case FilePart(key, filename, contentType, file) =>
                logger.info(
                  s"key = $key, filename = $filename, contentType = $contentType, file = $file")
                val size = Files.size(file.toPath)
                val path =
                  if (size > 0)
                    copyTempFile(file)
                  else null
                deleteTempFile(file)
                Option(path)
            }
            val userId =
              request.session.get(Constant.SESSION_USER_KEY).get.toLong
            repo.create(userId, entry.title, entry.content, fileUri).map { _ =>
              Redirect(routes.LandingPageController.showLandingPage())
                .flashing("success" -> "entry.created")
            }
          }
        )
      }

  // For test.
  def getEntries: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map { diaries =>
      Ok(Json.toJson(diaries))
    }
  }

  def list: Action[AnyContent] = Action.async { implicit request =>
    repo.getEntries.map { p =>
      Ok(views.html.list(p))
    }
  }

  def entry(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.getEntry(id).map { p =>
      val content = p.get
      Ok(views.html.entry(content))
    }
  }

  def image(entryId: Long): Action[AnyContent] = Action.async { implicit request =>
    val images = imgRepo.getImage(entryId)
    images.map { p =>
      val file = p.headOption.map { uri =>
        val file = new File(uri)
        file
      }
      file match {
        case Some(_) => Ok.sendFile(file.get)
        case None    => Ok.sendFile(new File("./Public/images/blank.png"))
      }
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
    val dest = Paths.get("/", "tmp", "mediasample", src.getFileName.toString)
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
