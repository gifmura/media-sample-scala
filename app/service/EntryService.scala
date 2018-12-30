package service
import com.google.inject.Singleton
import javax.inject.Inject
import jp.t2v.lab.play2.pager.{Pager, SearchResult}
import models.{Entry, EntryRepository, ImageRepository}
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntryService @Inject()(
    dbConfigProvider: DatabaseConfigProvider,
    entryRepo: EntryRepository,
    imgRepo: ImageRepository,
    cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  def create(user_id: Long,
             title: String,
             content: String,
             uri: Option[String],
             size: Option[Long]): Future[Any] = {
    uri match {
      case None => entryRepo.create(user_id, title, content)
      case Some(_) =>
        createEntryImage(user_id, title, content, uri.get, size.get)
    }
  }

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

}
