package models

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

class AccountRepositorySpec extends PlaySpec with MockitoSugar {

  val timestamp: Long = System.currentTimeMillis / 1000

  "AccountRepository#create" should{
    "return Future[Account] if name and password are not empty" in{
//      val app = new GuiceApplicationBuilder().build
//      val dbConfig = app.injector.instanceOf[DatabaseConfigProvider]

      lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
      lazy val injector: Injector = appBuilder.injector()
      lazy val dbConfProvider: DatabaseConfigProvider = injector.instanceOf[DatabaseConfigProvider]

      val model = new AccountRepository(dbConfProvider)
      val name = s"model-$timestamp"
      val password = "password"
      val result = model.create(name,password)
      result.onComplete{
        case Success(p) =>
          p.name must equal(name)
          p.password must equal(password)
      }
      result.map{p =>
        p.name must equal(name)
        p.password must equal(password)
      }
    }
  }
}
