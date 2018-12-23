import models._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.Mode
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.{Helpers, StubBodyParserFactory}
import service.EntryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EntryServiceSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory {

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

  lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy val dbConfProvider: DatabaseConfigProvider =
    injector.instanceOf[DatabaseConfigProvider]

  "EntryService#create" should {
    "return Future[Entry] if a uri of image is not specified" in {

      val id = 1
      val user_id = 1
      val title = "service-spec-title"
      val content = "service-spec-content"

      val mockedEntry: EntryRepository = mock[EntryRepository]
      when(mockedEntry.create(user_id, title, content)) thenReturn Future {
        new Entry(id, user_id, title, content)
      }
      val mockedImage: ImageRepository = mock[ImageRepository]
      val cc = stubMessagesControllerComponents()

      val service =
        new EntryService(dbConfProvider, mockedEntry, mockedImage, cc)
      val result = service.create(user_id, title, content, None, None)

      result mustBe a[Future[Entry]]
    }
  }

}
