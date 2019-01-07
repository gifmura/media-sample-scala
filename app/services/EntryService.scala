package services
import java.io.File
import java.nio.file.Files

import com.google.inject.Singleton
import javax.inject.Inject
import jp.t2v.lab.play2.pager.{Pager, SearchResult}
import models.{Entry, EntryRepository, ImageRepository}
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import services.amazon.S3Service
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntryService @Inject()(
    dbConfigProvider: DatabaseConfigProvider,
    entryRepo: EntryRepository,
    imgRepo: ImageRepository,
    cc: MessagesControllerComponents,
    config: Configuration,
    s3Service: S3Service)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  def findAll(pager: Pager[Entry]): Future[SearchResult[Entry]] = {
    var count: Int = 0
    entryRepo.countAll.map { p =>
      {
        p match {
          case _: Int => count = p
        }
      }
    }
    entryRepo.findAll(pager.allSorters, pager.limit, pager.offset).map { p =>
      SearchResult(pager, count) { _ =>
        p
      }
    }
  }

  def createEntry(userId: Long,
                  title: String,
                  content: String,
                  filePart: Option[FilePart[File]]): Future[Any] = {

    val imgFile = filePart.map {
      case FilePart(key, filename, contentType, file) =>
        logger.info(
          s"key = $key, filename = $filename, contentType = $contentType, file = $file")
        val size = Files.size(file.toPath)
        val uri =
          if (size > 0) {
            if (config.get[Boolean]("aws.s3.isEnabled")) {
              s3Service.uploadS3(file)
              file.getName
            } else
              uploadLocal(file)
          } else null
        deleteTempFile(file)
        (Option(uri), size)
    }

    val fileUri = imgFile.get._1

    fileUri match {
      case None => entryRepo.create(userId, title, content)
      case Some(uri) =>
        val size = imgFile.get._2
        createEntryImage(userId, title, content, uri, size)
    }

  }

  private def createEntryImage(user_Id: Long,
                               title: String,
                               content: String,
                               uri: String,
                               size: Long): Future[Long] = db.run {
    val action =
      for {
        newId <- entryRepo.getActionCreate(user_Id, title, content)
        _ <- imgRepo.getActionCreate(newId, uri, size)
      } yield newId
    action.transactionally
  }

  private def deleteTempFile(tmpFile: File) = {
    val src = tmpFile.toPath
    Files.deleteIfExists(src)
  }

  private def uploadLocal(tmpFile: File) = {
    val src = tmpFile.toPath
    val dest = ImageRepository.IMAGE_DIRECTORY.resolve(src.getFileName.toString)
    logger.info(s"dest = $dest")
    val newFile = Files.copy(src, dest)
    newFile.toString
  }
}
