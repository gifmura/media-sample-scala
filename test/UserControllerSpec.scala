import controllers.{AuthenticatedUserAction, UserController}
import models.{User, UserRepository}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers, StubBodyParserFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserControllerSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory
    with Results {

  val mockedAuthUserRepository: AuthenticatedUserAction =
    mock[AuthenticatedUserAction]

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

  "UserController#postUser" should {
    "should be SEE_OTHER" in {
      val id: Long = 1
      val email = "unit-test"
      val password = "password"
      val name = "sample-address@media-sample-scala.com"
      val mockedUserRepository: UserRepository = mock[UserRepository]
      when(mockedUserRepository.create(email, password, name)) thenReturn Future {
        new User(id, email, password, name)
      }
//      val cc = stubMessagesControllerComponents()

      val controller = new UserController(mockedUserRepository,
                                          stubMessagesControllerComponents(),
                                          mockedAuthUserRepository)
      val request =
        FakeRequest("POST", "/user").withJsonBody(Json.parse(s"""{
           |  "email": "$email",
           |  "password": "$password"
           |  "name": "$name",
           |}""".stripMargin))

      val result = controller.postUser.apply(request)
      val expectedFlash = Flash(Map("success" -> "user.created"))

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
    }

//    "throw RuntimeException if name or password is more than 20 letters" in{
//      val id:Long = 1
//      val name = "More than 20 letters."
//      val password = "password"
//      val mockedUserRepository: UserRepository = mock[UserRepository]
//      when(mockedUserRepository.create(name, password)) thenReturn Future {new User(id,name,password)}
//      val cc = stubMessagesControllerComponents()
//
//      val controller = new UserController(mockedUserRepository, cc, mockedAuthUserRepository)
//      val request = FakeRequest("POST", "/user").withJsonBody(Json.parse(
//        s"""{
//           |  "name": "$name",
//           |  "password": "$password"
//           |}""".stripMargin))
//
//      a[RuntimeException] must be thrownBy {
//        controller.postUser.apply(request)
//      }
//    }
  }

}
