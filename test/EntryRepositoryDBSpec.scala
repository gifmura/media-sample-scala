import models.EntryRepository
import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class EntryRepositoryDBSpec extends PlaySpec {

  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider = injector.instanceOf[DatabaseConfigProvider]

  val model = new EntryRepository(dbConfProvider)
  val timestamp: Long = System.currentTimeMillis / 1000
  // userId = 1 is for testing.
  val userId = 1;
  val title = s"DB-Spec-title-$timestamp"
  val content = "DB-Spec-content-$timestamp"

  var entry_id: Long = -1;
  "EntryRepository#create" should {
    "register an entry if user_id, title and content are correct values" in {
      val result = model.create(userId, title, content)
      result.map { p =>
        assert(p.id > 0)
        assert(p.user_id == userId)
        assert(p.title == title)
        assert(p.content == content)
        entry_id = p.id
      }
    }
  }

  "EntryRepository#getEntries" should {
    "return some entries if they were already registered" in {
      val result = model.getEntries
      result.map { p =>
        assert(p.length > 0)
      }
    }
  }

  "EntryRepository#getEntriy" should {
    "return an entry if it was already registered" in {
      val result = model.getEntry(entry_id)
      result.map { p =>
        val entry = p.headOption
        assert(entry != None)
        entry.map{p =>
          assert(p.id == entry_id)
          assert(p.title.length > 0)
          assert(p.content.length > 0)
        }
      }
    }
  }
}
