package models

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiaryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec:ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DiaryTable(tag: Tag) extends Table[Diary](tag, "diary"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accountId = column[Long]("accountId")
    def imageId = column[Option[Long]]("imageId")
    def title = column[String]("title")
    def body = column[String]("body")
    def * = (id, accountId, imageId, title, body) <> ((Diary.apply _).tupled, Diary.unapply)
  }

  private val diaries = TableQuery[DiaryTable]

  def create(accountId:Long, imageId: Option[Long], title: String, body: String):Future[Diary] = db.run{
    (diaries.map(p => (p.accountId, p.imageId, p.title, p.body))
      returning diaries.map(_.id)
      into ((titleBody, id) => Diary(id, titleBody._1, titleBody._2, titleBody._3, titleBody._4))
      ) += (accountId, imageId, title, body)
  }

  def list(): Future[Seq[Diary]] = db.run{
    diaries.result
  }

  def getEntries() = db.run{
    diaries.map(p => (p.id, p.title)).result
  }

  def getEntry(id:Long): Future[Seq[Diary]] = db.run{
    diaries.filter(p => p.id === id).result
  }
}