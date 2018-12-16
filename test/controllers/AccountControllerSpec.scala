package models

import controllers.{AccountController, AuthenticatedAccountAction}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers, StubBodyParserFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AccountControllerSpec extends PlaySpec with MockitoSugar with StubBodyParserFactory with Results {

  val mockedAuthAccountRepository: AuthenticatedAccountAction = mock[AuthenticatedAccountAction]

  def stubMessagesControllerComponents() : MessagesControllerComponents = {
    val stub = Helpers.stubControllerComponents()
    new DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty),stub.messagesApi)(stub.executionContext),
      DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext), stub.parsers, stub.messagesApi, stub.langs, stub.fileMimeTypes,
      stub.executionContext
    )
  }

  "AccountController#create" should{
    "should be SEE_OTHER" in{
      val id:Long = 1
      val name = "unit-test"
      val password = "password"
      val mockedAccountRepository: AccountRepository = mock[AccountRepository]
      when(mockedAccountRepository.create(name, password)) thenReturn Future {new Account(id,name,password)}
      val cc = stubMessagesControllerComponents()

      val controller = new AccountController(mockedAccountRepository,cc, mockedAuthAccountRepository)
      val request = FakeRequest("POST", "/account").withJsonBody(Json.parse(
        s"""{
           |  "name": "$name",
           |  "password": "$password"
           |}""".stripMargin))

      val result = controller.addAccount.apply(request)
      val expectedFlash = Flash(Map("success" -> "account.created"))

      status(result) must equal(SEE_OTHER)
      flash(result) must equal(expectedFlash)
    }
  }
}
