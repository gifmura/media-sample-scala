package models

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec:ExecutionContext){
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class AccountsTable(tag: Tag) extends Table[Account](tag, "account"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id, name) <> ((Account.apply _).tupled, Account.unapply)
  }

  private val accounts = TableQuery[AccountsTable]

  def create(name: String):Future[Account] = db.run{
    (accounts.map(p =>(p.name))
      returning accounts.map(_.id)
      into ((_name, id) => Account(id, _name))
      ) += (name)
  }

  def list(): Future[Seq[Account]] = db.run {
    accounts.result
  }
}
