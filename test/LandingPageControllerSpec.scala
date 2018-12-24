import controllers.LandingPageController
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test.{WithApplication, _}

import scala.concurrent.ExecutionContext.Implicits.global

class LandingPageControllerSpec
    extends PlaySpec
    with StubBodyParserFactory
    with Results {

  val cc: MessagesControllerComponents = stubMessagesControllerComponents()

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

  "LandingPageController#showLandingPage" should {
    "should be OK" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val controller =
        new LandingPageController(cc)
      val request: RequestHeader = FakeRequest().withCSRFToken
      val result = controller.showLandingPage().apply(request)

      status(result) mustBe OK
    }
  }

}
