package service
import com.google.inject.Singleton
import javax.inject.Inject
import models.{EntryRepository, ImageRepository}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntryService @Inject()(dbConfigProvider: DatabaseConfigProvider,
                              entryRepo: EntryRepository,
                              imgRepo: ImageRepository,
                              cc: MessagesControllerComponents
                            )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  def create(user_id: Long,title: String,
             content: String,
             uri: Option[String],
             size: Option[Long]): Future[Any] = {
    uri match {
      case None    => entryRepo.createEntry(user_id, title, content)
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
        newId <- entryRepo.getActionCreate(user_Id, title, content)
        _ <- imgRepo.getActionCreate(newId, uri, size)
      } yield newId
    action.transactionally
  }

}
