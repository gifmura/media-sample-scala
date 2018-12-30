import models.Constant
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.Json
import play.api.mvc
import play.api.mvc.{AnyContentAsJson, Flash, Session}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Application" should {

    val userId: Long = 1
    val timestamp: Long = System.currentTimeMillis / 1000

    "send 404 on a bad request" in new WithApplication {
      route(app, FakeRequest(GET, "/none-page")) must beSome.which(
        status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication {
      val home: Future[mvc.Result] = route(app, FakeRequest(GET, "/")).get

      status(home) must equalTo(SEE_OTHER)
    }

    "render the register page" in new WithApplication() {
      val register: Future[mvc.Result] =
        route(app, FakeRequest(GET, "/register")).get

      status(register) must equalTo(OK)
      contentType(register) must beSome.which(_ == "text/html")
    }

    "render the login page" in new WithApplication() {
      val login: Future[mvc.Result] = route(app, FakeRequest(GET, "/login")).get

      status(login) must equalTo(OK)
      contentType(login) must beSome.which(_ == "text/html")
    }

    "render the logout page" in new WithApplication() {
      val logout: Future[mvc.Result] =
        route(app,
              FakeRequest(GET, "/logout").withSession(
                Constant.SESSION_USER_KEY -> userId.toString)).get
      val expectedSession = Session()

      status(logout) must equalTo(SEE_OTHER)
      session(logout) must equalTo(expectedSession)
    }

    "render the list page" in new WithApplication() {
      val list: Future[mvc.Result] = route(app, FakeRequest(GET, "/list")).get

      status(list) must equalTo(OK)
      contentType(list) must beSome.which(_ == "text/html")
    }

    "render the edit page" in new WithApplication() {
      val edit: Future[mvc.Result] =
        route(app,
              FakeRequest(GET, "/edit").withSession(
                Constant.SESSION_USER_KEY -> userId.toString)).get

      status(edit) must equalTo(OK)
      contentType(edit) must beSome.which(_ == "text/html")
    }

    "render the 1st entry page" in new WithApplication() {
      val entry: Future[mvc.Result] =
        route(app,
              FakeRequest(GET, "/entry/1").withSession(
                Constant.SESSION_USER_KEY -> userId.toString)).get

      status(entry) must equalTo(OK)
      contentType(entry) must beSome.which(_ == "text/html")
    }

    "render the landing page" in new WithApplication() {
      val landing: Future[mvc.Result] =
        route(app, FakeRequest(GET, "/landing")).get

      status(landing) must equalTo(OK)
      contentType(landing) must beSome.which(_ == "text/html")
    }

    val email = "dummy-address@media-sample-scala.com"
    val password = "password"

    "post a new user" in new WithApplication {
      val request: FakeRequest[AnyContentAsJson] =
        FakeRequest("POST", "/postUser").withJsonBody(Json.parse(s"""{
           |  "email": "$email",
           |  "password": "$password",
           |  "name": "integration-test-user"
           |}""".stripMargin))

      val user: Future[mvc.Result] = route(app, request).get
      val expectedFlash: Flash = Flash(Map("success" -> "user.created"))

      status(user) must equalTo(SEE_OTHER)
      flash(user) must equalTo(expectedFlash)
    }

    "attempt a login request" in new WithApplication {
      val request: FakeRequest[AnyContentAsJson] =
        FakeRequest("POST", "/attempt").withJsonBody(Json.parse(s"""{
           |  "email": "$email",
           |  "password": "$password"
           |}""".stripMargin))

      val attempt: Future[mvc.Result] = route(app, request).get
      val expectedFlash = Flash(Map("success" -> "You are logged in."))
      val expectedSession =
        Session(Map(Constant.SESSION_USER_KEY -> userId.toString))

      status(attempt) must equalTo(SEE_OTHER)
      flash(attempt) must equalTo(expectedFlash)
      session(attempt) must equalTo(expectedSession)
    }

  }
}
