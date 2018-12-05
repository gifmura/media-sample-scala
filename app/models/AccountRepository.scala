package models

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class AccountsTable(tag: Tag) extends Table[Account](tag, "account") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def password = column[String]("password")
    def * = (id, name, password) <> ((Account.apply _).tupled, Account.unapply)
  }

  private val accounts = TableQuery[AccountsTable]

  def create(name: String, password: String): Future[Account] = db.run {
    (accounts.map(p => (p.name, p.password))
      returning accounts.map(_.id)
      into ((namePassword, id) => Account(id, namePassword._1, namePassword._2))) += (name, password)
  }

  def list(): Future[Seq[Account]] = db.run {
    accounts.result
  }

  def getId(name: String, password: String) = db.run(
    accounts
      .filter(i => i.name === name && i.password === password)
      .map(p => p.id)
      .result
      .headOption
  )
}
