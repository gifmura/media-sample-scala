package models

import java.sql.Timestamp
import java.util.Date

import com.google.inject.{Inject, Singleton}
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class UsersTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email", O.Unique)
    def password = column[String]("password")
    def name = column[String]("name", O.Unique)
    def user_type = column[String]("user_type", O.Default(User.TYPE_NORMAL))
    def registration_time =
      column[Timestamp]("registration_time",
                        O.Default(new Timestamp(new Date().getTime)))
    def status = column[String]("status", O.Default(User.STATUS_ACTIVE))
    def * =
      (id, email, password, name, user_type, registration_time, status) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UsersTable]

  def create(email: String,
             password: String,
             name: String,
             user_type: String = User.TYPE_NORMAL): Future[User] = db.run {
    val hashedPassword = getHashedPassword(password)
    (users.map(p => (p.email, p.password, p.name, p.user_type))
      returning users.map(_.id)
      into ((emailPassword,
             id) =>
              User(
                id,
                emailPassword._1,
                emailPassword._2,
                emailPassword._3,
                emailPassword._4))) += (email, hashedPassword, name, user_type)
  }

  def getId(email: String, password: String): Future[Option[Long]] = db.run {
    users
      .filter(i => i.email === email)
      .map(p => (p.id, p.password))
      .result
      .headOption
      .map {
        case Some(s) =>
          if (BCrypt.checkpw(password, s._2)) Option(s._1)
          else None
        case None => None
      }
  }

  private def getHashedPassword(password: String): String = {
    val salt = BCrypt.gensalt()
    BCrypt.hashpw(password, salt)
  }

}
