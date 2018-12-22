package models

import java.sql.Timestamp
import java.util.Date

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntryRepository @Inject()(
    dbConfigProvider: DatabaseConfigProvider,
    imageRepository: ImageRepository)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class EntryTable(tag: Tag) extends Table[Entry](tag, "entry") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def user_id = column[Long]("user_Id")
    def title = column[String]("title")
    def content = column[String]("content")
    def create_time =
      column[Timestamp]("create_time",
        O.Default(new Timestamp(new Date().getTime)))
    def update_time =
      column[Timestamp]("update_time",
        O.Default(new Timestamp(new Date().getTime)))
    def status = column[String]("status", O.Default(Entry.STATUS_ACTIVE))
    def * =
      (id, user_id, title, content, create_time, update_time, status) <> ((Entry.apply _).tupled, Entry.unapply)
  }

  private val entries = TableQuery[EntryTable]

  def list(): Future[Seq[Entry]] = db.run {
    entries.result
  }

  def getEntries: Future[Seq[
    (Long, String)
  ]] = db.run {
    entries.map(p => (p.id, p.title)).result
  }

  def getEntry(id: Long): Future[Option[Entry]] = db.run {
    entries.filter(p => p.id === id).result.headOption
  }

  def create(user_id: Long,title: String,
             content: String,
             uri: Option[String],
             size: Option[Long]): Future[Any] = {
    uri match {
      case None    => createEntry(user_id, title, content)
      case Some(_) => createEntryImage(user_id, title, content, uri.get, size.get)
    }
  }

  def createEntryImage(user_Id: Long,
                       title: String,
                       content: String,
                       uri: String,
                       size: Long): Future[Long] = db.run {
    val action =
      for {
        newId <- (entries returning entries.map(_.id)) += Entry(0,
                                                                user_Id,
                                                                title,
                                                                content
          )
        _ <- imageRepository.create(newId, uri, size)
      } yield newId
    action.transactionally
  }

  def createEntry(user_Id: Long, title: String, content: String): Future[Entry] =
    db.run {
      (entries.map(p => (p.user_id, p.title, p.content))
        returning entries.map(_.id)
        into (
            (titleContent,
             id) =>
              Entry(id, titleContent._1, titleContent._2, titleContent._3))) += (user_Id, title, content)
    }

}
