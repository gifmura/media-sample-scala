package models

import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class ImageRepositoryDBSpec extends PlaySpec {

  lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider =
    injector.instanceOf[DatabaseConfigProvider]

  val model = new ImageRepository(dbConfProvider)
  val entryId = 1 // Please set the Entry Id registered with images.

  "ImageRepository#getImage" should {
    "return an uri of the image if it was already registered" in {
      val result = model.getImage(entryId)
      result.map { p =>
        val image = p.headOption
        assert(image.isDefined)
        image.map { i =>
          assert(i.length > 0)
        }
      }
    }
  }

}
