package models

import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class AccountRepositorySpec extends PlaySpec {

  lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider =
    injector.instanceOf[DatabaseConfigProvider]

  val timestamp: Long = System.currentTimeMillis / 1000
  val model = new AccountRepository(dbConfProvider)
  val name = s"model2-$timestamp"
  val password = "password"

  "AccountRepository#create" should {
    "return account if name and password are correct values" in {
      val result = model.create(name, password)
      result.map { p =>
        assert(p.name == name)
        assert(p.password == password)
        id = p.id
      }
    }
  }

  var id: Long = -1

  "AccountRepository#getId" should {
    "return account id if existing name and password specified" in {
      val result = model.getId(name, password)
      result.map { p =>
        assert(p.value == id)
      }
    }
  }
}
