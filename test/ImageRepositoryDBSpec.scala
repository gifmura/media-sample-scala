import models.ImageRepository
import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class ImageRepositoryDBSpec extends PlaySpec {

  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider = injector.instanceOf[DatabaseConfigProvider]

  val model = new ImageRepository(dbConfProvider)
  // entryId = 1 is for testing.
  val entryId = 1;
  "ImageRepository#getImage" should {
    "return an uri of the image if it was already registered" in {
      val result = model.getImage(entryId)
      result.map { p =>
        val image = p.headOption
        assert(image != None)
        image.map{i =>
          assert(i.length > 0)
        }
      }
    }
  }

}
