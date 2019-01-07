package controllers

import java.io.{File, InputStream}

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.mvc._
import services.amazon.S3Service

import scala.concurrent.ExecutionContext

class ImageController @Inject()(
    repo: ImageRepository,
    cc: MessagesControllerComponents,
    config: Configuration,
    s3Service: S3Service
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  def image(entryId: Long): Action[AnyContent] = Action.async {
    implicit request =>
      val images = repo.getImage(entryId)
      images.map { p =>
        val file = p.headOption.map { uri =>
          if (config.get[Boolean]("aws.s3.isEnabled")) {
            val s3is = s3Service.downloadS3(uri)
            s3is
          } else {
            val file = new File(uri)
            file
          }
        }
        file match {
          case None          => Ok.sendFile(new File("./Public/images/blank.png"))
          case Some(f: File) => Ok.sendFile(f)
          case Some(i: InputStream) => {
            val dataContent: Source[ByteString, _] =
              StreamConverters.fromInputStream(() => i)
            Ok.chunked(dataContent)
          }
        }
      }
  }

}
