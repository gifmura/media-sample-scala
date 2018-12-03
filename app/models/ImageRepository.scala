package models

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImageRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class ImageTable(tag: Tag) extends Table[Image](tag, "image") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def entryId = column[Long]("entryId")
    def url = column[String]("url")
    def * = (id, entryId, url) <> ((Image.apply _).tupled, Image.unapply)
  }

  private val images = TableQuery[ImageTable]

  def create(entryId: Long, url: String) = {
    (images.map(p => (p.entryId, p.url))
      returning images.map(_.id)
      into ((entryIdUrl, id) => Image(id, entryIdUrl._1, entryIdUrl._2))) += (entryId, url)
  }

  def getImage(entryId: Long): Future[Seq[String]] = db.run {
    images.filter(p => p.entryId === entryId).map(p => p.url).result
  }

}
