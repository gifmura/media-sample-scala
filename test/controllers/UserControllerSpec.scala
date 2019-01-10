package controllers

import akka.util.ByteString
import models.{User, UserRepository}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import services.session.SessionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserControllerSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory
    with Results {

  val mockedUserRepository: UserRepository = mock[UserRepository]

  val cc: MessagesControllerComponents = stubMessagesControllerComponents()

  val mockedUserAction: UserInfoAction = mock[UserInfoAction]

  val mockedSessionService: SessionService = mock[SessionService]

  val mockedSessionGenerator: SessionGenerator = mock[SessionGenerator]

  def stubMessagesControllerComponents(): MessagesControllerComponents = {
    val stub = Helpers.stubControllerComponents()
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(
        stubBodyParser(AnyContentAsEmpty),
        stub.messagesApi)(stub.executionContext),
      DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
      stub.parsers,
      stub.messagesApi,
      stub.langs,
      stub.fileMimeTypes,
      stub.executionContext
    )
  }

  "UserController#register" should {
    "should be OK" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           mockedUserAction,
                           mockedSessionService,
                           mockedSessionGenerator)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.register.apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }

  "UserController#postUser" should {
    "should be SEE_OTHER" in {
      val userId = 1
      val email = "sample-address@media-sample-scala.com"
      val password = "password"
      val name = "unit-test"

      when(mockedUserRepository.create(email, password, name)) thenReturn Future {
        new User(userId, email, password, name)
      }

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           mockedUserAction,
                           mockedSessionService,
                           mockedSessionGenerator)
      val request =
        FakeRequest("POST", "/postUser").withJsonBody(Json.parse(s"""{
                                                                    |  "email": "$email",
                                                                    |  "password": "$password",
                                                                    |  "name": "$name"
                                                                    |}""".stripMargin))

      val result = controller.postUser.apply(request)
      val expectedFlash = Flash(Map("success" -> "user.created"))

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
    }
  }

  "UserController#login" should {
    "should be OK" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           mockedUserAction,
                           mockedSessionService,
                           mockedSessionGenerator)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.login.apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }

  "UserController#attempt" should {
    "should be SEE_OTHER with new session if the user has already registered" in {
      val userId = 1L
      val email = "sample-address@media-sample-scala.com"
      val password = "password"

      when(mockedUserRepository.getId(email, password)) thenReturn Future(
        Option(userId))

      val sessionId = 1.toString
      val cookie = Cookie("userId", userId.toString)
      when(mockedSessionGenerator.createSession(UserInfo(userId.toString))) thenReturn Future(
        (sessionId, cookie))

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           mockedUserAction,
                           mockedSessionService,
                           mockedSessionGenerator)

      val request =
        FakeRequest().withJsonBody(Json.parse(s"""{
                                              |  "email": "$email",
                                              |  "password": "$password"
                                              |}""".stripMargin))

      val result = controller.attempt.apply(request)
      val expectedFlash = Flash(Map(FLASH_SUCCESS -> "You are logged in."))

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
      assert(session(result).get(SESSION_ID).nonEmpty)
    }
  }

  "UserController#attempt" should {
    "should be OK with errorForm if email or password is empty" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val userId = 1L
      val email = ""
      val password = "password"

      when(mockedUserRepository.getId(email, password)) thenReturn Future(
        Option(userId))

      val sessionId: String = 1.toString
      val cookie = Cookie("userId", userId.toString)
      when(mockedSessionGenerator.createSession(UserInfo(userId.toString))) thenReturn Future(
        (sessionId, cookie))

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           mockedUserAction,
                           mockedSessionService,
                           mockedSessionGenerator)

      val request: RequestHeader =
        FakeRequest()
          .withJsonBody(Json.parse(s"""{
                                                 |  "email": "$email",
                                                 |  "password": "$password"
                                                 |}""".stripMargin))
          .withCSRFToken

      val result: Accumulator[ByteString, Result] =
        controller.attempt.apply(request)
      val expectedFlash = Flash(Map(FLASH_SUCCESS -> "You are logged in."))

      status(result) must equal(OK)
      contentType(result) mustBe Some("text/html")

    }
  }

  "UserController#attempt" must {
    "must not start new session if the user has not registered" in {
      val email = "dummy-address@media-sample-scala.com"
      val password = "dummy-password"

      when(mockedUserRepository.getId(email, password)) thenReturn Future(None)

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           mockedUserAction,
                           mockedSessionService,
                           mockedSessionGenerator)
      val request =
        FakeRequest().withJsonBody(Json.parse(s"""{
                                                 |  "email": "$email",
                                                 |  "password": "$password"
                                                 |}""".stripMargin))

      val result = controller.attempt.apply(request)
      val expectedFlash = Flash(Map("error" -> "Invalid name/password."))
      val expectedSession = Session(Map())

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
      session(result) must equal(expectedSession)
    }
  }

  "UserController#logout" should {
    "should Be SEE_OTHER" in {

      val sessionId = 1.toString
      val request =
        FakeRequest()
          .withSession((SESSION_ID, sessionId))

      val app = new GuiceApplicationBuilder().build
      val dummyUserAction = app.injector.instanceOf[DummyUserInfoAction]

      when(mockedSessionService.delete(sessionId)) thenReturn Future {}

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           dummyUserAction,
                           mockedSessionService,
                           mockedSessionGenerator)

      val result = controller.logout.apply(request)
      val expectedFlash = Flash(Map(FLASH_SUCCESS -> "You are logged out."))

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
    }
  }

  "UserController#logout" should {
    "should be FORBIDDEN if the user did not logged in" in {

      val request =
        FakeRequest()
          .withSession((SESSION_ID, 1.toString))

      val app = new GuiceApplicationBuilder().build
      val userAction = app.injector.instanceOf[UserInfoAction]

      val controller =
        new UserController(mockedUserRepository,
                           cc,
                           userAction,
                           mockedSessionService,
                           mockedSessionGenerator)

      val result = controller.logout.apply(request)

      status(result) must equal(FORBIDDEN)
    }
  }

}
