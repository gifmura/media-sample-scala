package models

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, imageRepository: ImageRepository)(implicit ec:ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class EntryTable(tag: Tag) extends Table[Entry](tag, "entry"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accountId = column[Long]("accountId")
    def title = column[String]("title")
    def body = column[String]("body")
    def * = (id, accountId, title, body) <> ((Entry.apply _).tupled, Entry.unapply)
  }

  private val entries = TableQuery[EntryTable]

  def list(): Future[Seq[Entry]] = db.run{
    entries.result
  }

  def getEntries() = db.run{
    entries.map(p => (p.id, p.title)).result
  }

  def getEntry(id:Long): Future[Seq[Entry]] = db.run{
    entries.filter(p => p.id === id).result
  }

  def create(accountId:Long, title:String, body:String, url:Option[String]) = {
    url match {
      case None => createEntry(accountId, title, body)
      case Some(_) => createEntryImage(accountId, title, body, url.get)
    }
  }

  def createEntryImage(accountId:Long, title:String, body:String, url:String) = db.run{
    val action =
      for {
        newId <- (entries returning entries.map(_.id))+= Entry(0, accountId, title, body)
        _ <- imageRepository.create(newId, url)
      } yield newId
    action.transactionally
  }

  def createEntry(accountId:Long, title: String, body: String):Future[Entry] = db.run{
    (entries.map(p => (p.accountId, p.title, p.body))
      returning entries.map(_.id)
      into ((titleBody, id) => Entry(id, titleBody._1, titleBody._2, titleBody._3))
      ) += (accountId, title, body)
  }

}