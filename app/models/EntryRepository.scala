package models

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec:ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class EntryTable(tag: Tag) extends Table[Entry](tag, "entry"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accountId = column[Long]("accountId")
    def imageUrl = column[Option[String]]("imageUrl")
    def title = column[String]("title")
    def body = column[String]("body")
    def * = (id, accountId, imageUrl, title, body) <> ((Entry.apply _).tupled, Entry.unapply)
  }

  private val entries = TableQuery[EntryTable]

  def create(accountId:Long, imageUrl: Option[String], title: String, body: String):Future[Entry] = db.run{
    (entries.map(p => (p.accountId, p.imageUrl, p.title, p.body))
      returning entries.map(_.id)
      into ((titleBody, id) => Entry(id, titleBody._1, titleBody._2, titleBody._3, titleBody._4))
      ) += (accountId, imageUrl, title, body)
  }

  def list(): Future[Seq[Entry]] = db.run{
    entries.result
  }

  def getEntries() = db.run{
    entries.map(p => (p.id, p.title)).result
  }

  def getEntry(id:Long): Future[Seq[Entry]] = db.run{
    entries.filter(p => p.id === id).result
  }
}