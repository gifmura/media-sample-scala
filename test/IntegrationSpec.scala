import models.Constant
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.Json
import play.api.mvc.{Flash, Session}
import play.api.test.Helpers._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Application" should {

    val accountId: Long = 1;
    val timestamp: Long = System.currentTimeMillis / 1000

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

    "render the logout page" in new WithApplication() {
      val logout = route(app, FakeRequest(GET, "/logout").withSession(Constant.SESSION_ACCOUNTID_KEY -> accountId.toString)).get
      val expectedSession = Session()

      status(logout) must equalTo(SEE_OTHER)
      session(logout) must equalTo(expectedSession)
    }

    "render the list page" in new WithApplication() {
      val list = route(app, FakeRequest(GET, "/list")).get

      status(list) must equalTo(OK)
      contentType(list) must beSome.which(_ == "text/html")
    }

    "render the edit page" in new WithApplication() {
      val edit = route(app, FakeRequest(GET, "/edit").withSession(Constant.SESSION_ACCOUNTID_KEY -> accountId.toString)).get

      status(edit) must equalTo(OK)
      contentType(edit) must beSome.which(_ == "text/html")
    }

    "render the 1st entry page" in new WithApplication() {
      val entry = route(app, FakeRequest(GET, "/entry/1").withSession(Constant.SESSION_ACCOUNTID_KEY -> accountId.toString)).get

      status(entry) must equalTo(OK)
      contentType(entry) must beSome.which(_ == "text/html")
    }

    "render the landing page" in new WithApplication() {
      val landing = route(app, FakeRequest(GET, "/landing")).get

      status(landing) must equalTo(OK)
      contentType(landing) must beSome.which(_ == "text/html")
    }

    "post a new account" in new WithApplication{
      val request = FakeRequest("POST", "/account").withJsonBody(Json.parse(
        s"""{
           |  "name": "spec-$timestamp",
           |  "password": "password"
           |}""".stripMargin))

      val account = route(app, request).get
      val expectedFlash = Flash(Map("success" -> "account.created"))

      status(account) must equalTo(SEE_OTHER)
      flash(account) must equalTo(expectedFlash)
    }

    "attempt a login request" in new WithApplication{
      val request = FakeRequest("POST", "/attempt").withJsonBody(Json.parse(
        s"""{
           |  "name": "hogehoge",
           |  "password": "password"
           |}""".stripMargin))

      val attempt = route(app, request).get
      val expectedFlash = Flash(Map("success" -> "You are logged in."))
      val expectedSession = Session(Map(Constant.SESSION_ACCOUNTID_KEY -> accountId.toString))

      status(attempt) must equalTo(SEE_OTHER)
      flash(attempt) must equalTo(expectedFlash)
      session(attempt) must equalTo(expectedSession)
    }

  }
}
