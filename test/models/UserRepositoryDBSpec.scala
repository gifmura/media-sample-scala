package models

import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositoryDBSpec extends PlaySpec {

  lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider =
    injector.instanceOf[DatabaseConfigProvider]

  val model = new UserRepository(dbConfProvider)
  val timestamp: Long = System.currentTimeMillis / 1000
  val email = s"address-$timestamp@media-sample-scala.com"
  val password = "password"
  val name = s"model2-$timestamp"

  "UserRepository#create" should {
    "return user if email, password and name are correct values" in {
      val result = model.create(email, password, name)
      result.map { p =>
        assert(p.id > 0)
        assert(p.email == email)
        assert(p.password == password)
        assert(p.name == name)
      }
    }
  }

  "UserRepository#getId" should {
    "return user id if existing email and password specified" in {
      val result = model.getId(name, password)
      result.map { p =>
        assert(p.value > 0)
      }
    }
  }
}
