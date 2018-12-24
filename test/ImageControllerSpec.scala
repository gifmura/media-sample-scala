import controllers.ImageController
import models.ImageRepository
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test.{WithApplication, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageControllerSpec
    extends PlaySpec
    with MockitoSugar
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

  "ImageController#image" should {
    "should be OK if the entryId was registered" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val entryId = 1
      val mockedUserRepository: ImageRepository = mock[ImageRepository]
      when(mockedUserRepository.getImage(entryId)) thenReturn Future {
        Seq("./Public/images/blank.png")
      }
      val controller =
        new ImageController(mockedUserRepository, cc)
      val request: RequestHeader = FakeRequest().withCSRFToken
      val result = controller.image(entryId).apply(request)

      status(result) mustBe OK
      contentAsString(result).length > 0
    }
  }

}
