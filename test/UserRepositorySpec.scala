import models.UserRepository
import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositorySpec extends PlaySpec {

  lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider =
    injector.instanceOf[DatabaseConfigProvider]

  val timestamp: Long = System.currentTimeMillis / 1000
  val model = new UserRepository(dbConfProvider)
  val email = s"address-$timestamp@media-sample-scala.com"
  val password = "password"
  val name = s"model2-$timestamp"

  "UserRepository#create" should {
    "return user if name and password are correct values" in {
      val result = model.create(email, password, name)
      result.map { p =>
        assert(p.name == name)
        assert(p.password == password)
        id = p.id
      }
    }
  }

  var id: Long = -1

  "UserRepository#getId" should {
    "return user id if existing name and password specified" in {
      val result = model.getId(name, password)
      result.map { p =>
        assert(p.value == id)
      }
    }
  }
}
