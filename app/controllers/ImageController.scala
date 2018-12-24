package controllers

import java.io.File

import javax.inject.Inject
import models._
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ImageController @Inject()(
    repo: ImageRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  def image(entryId: Long): Action[AnyContent] = Action.async {
    implicit request =>
      val images = repo.getImage(entryId)
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
}
