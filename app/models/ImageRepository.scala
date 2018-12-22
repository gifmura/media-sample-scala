package models

import java.nio.file.{Files, Path, Paths}

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

object ImageRepository{
  val BASE_DIRECTORY: Path = Paths.get("/", "tmp")
  val IMAGE_DIRECTORY: Path = Paths.get("/", "tmp", "media-sample-scala")
}

@Singleton
class ImageRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val baseDir: Path = ImageRepository.BASE_DIRECTORY
  val imageDir: Path = ImageRepository.IMAGE_DIRECTORY
  if (Files.notExists(baseDir)) Files.createDirectory(baseDir)
  if (Files.notExists(imageDir)) Files.createDirectories(imageDir)

  import dbConfig._
  import profile.api._

  private class ImageTable(tag: Tag) extends Table[Image](tag, "image") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def entry_id = column[Long]("entry_id")
    def uri = column[String]("uri", O.Unique)
    def size = column[Long]("size")
    def * = (id, entry_id, uri, size) <> ((Image.apply _).tupled, Image.unapply)
  }

  private val images = TableQuery[ImageTable]

  def create(entry_id: Long, uri: String, size: Long
  ) = {
    (images.map(p => (p.entry_id, p.uri, p.size))
      returning images.map(_.id)
      into ((entryIdUrl, id) => Image(id, entryIdUrl._1, entryIdUrl._2, entryIdUrl._3))) += (entry_id, uri, size)
  }

  def getImage(entry_id: Long): Future[Seq[String]] = db.run {
    images.filter(p => p.entry_id === entry_id).map(p => p.uri).result
  }

}
