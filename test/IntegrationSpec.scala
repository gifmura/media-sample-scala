import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(app, FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    "render the register page" in new WithApplication() {
      val register = route(app, FakeRequest(GET, "/register")).get

      status(register) must equalTo(OK)
      contentType(register) must beSome.which(_ == "text/html")
    }

    "render the login page" in new WithApplication() {
      val login = route(app, FakeRequest(GET, "/login")).get

      status(login) must equalTo(OK)
      contentType(login) must beSome.which(_ == "text/html")
    }

    "render the list page" in new WithApplication() {
      val list = route(app, FakeRequest(GET, "/list")).get

      status(list) must equalTo(OK)
      contentType(list) must beSome.which(_ == "text/html")
    }

    "render the langing page" in new WithApplication() {
      val langing = route(app, FakeRequest(GET, "/langing")).get

      status(langing) must equalTo(OK)
      contentType(langing) must beSome.which(_ == "text/html")
    }

  }
}
