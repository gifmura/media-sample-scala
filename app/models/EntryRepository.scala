package models

import java.sql.Timestamp
import java.util.Date

import com.google.inject.{Inject, Singleton}
import jp.t2v.lab.play2.pager.{OrderType, Sorter}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
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

  def getEntries: Future[Seq[
    (Long, String)
  ]] = db.run {
    entries.map(p => (p.id, p.title)).result
  }

  def countAll: Future[Int] = db.run {
    entries.length.result
  }

  def findAll(orders: Seq[Sorter[Entry]],
              limit: Int,
              offset: Int): Future[Seq[Entry]] = db.run {
    sortEntry(entries, orders)
      .drop(offset)
      .take(limit)
      .result
  }

  private def sortEntry(entry: TableQuery[EntryTable],
                        orders: Seq[Sorter[Entry]])
    : Query[EntryTable, EntryTable#TableElementType, Seq] = {
    val order = orders.headOption

    order match {
      case None => entry.sortBy(_.id.desc)
      case Some(o) =>
        o.dir match {
          case OrderType.Ascending =>
            o.key match {
              case Entry.SORT_KEY_ID     => entry.sortBy(_.id.asc)
              case Entry.SORT_KEY_TITLE  => entry.sortBy(_.title.asc)
              case Entry.SORT_KEY_CREATE => entry.sortBy(_.create_time.asc)
              case Entry.SORT_KEY_UPDATE => entry.sortBy(_.update_time.asc)
            }
          case OrderType.Descending =>
            o.key match {
              case Entry.SORT_KEY_ID     => entry.sortBy(_.id.desc)
              case Entry.SORT_KEY_TITLE  => entry.sortBy(_.title.desc)
              case Entry.SORT_KEY_CREATE => entry.sortBy(_.create_time.desc)
              case Entry.SORT_KEY_UPDATE => entry.sortBy(_.update_time.desc)
            }
        }
    }
  }

  def getEntry(id: Long): Future[Option[Entry]] = db.run {
    entries.filter(p => p.id === id).result.headOption
  }

  def create(user_Id: Long, title: String, content: String): Future[Entry] =
    db.run {
      (entries.map(p => (p.user_id, p.title, p.content))
        returning entries.map(_.id)
        into (
            (titleContent,
             id) =>
              Entry(id, titleContent._1, titleContent._2, titleContent._3))) += (user_Id, title, content)
    }

  def getActionCreate(user_Id: Long, title: String, content: String) =
    (entries returning entries.map(_.id)) += Entry(0, user_Id, title, content)
}
