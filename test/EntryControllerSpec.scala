import java.io.File

import controllers.{AuthenticatedUserAction, EntryController}
import models.{Constant, Entry, EntryRepository}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import service.EntryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EntryControllerSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory
    with Results {

  val cc: MessagesControllerComponents = stubMessagesControllerComponents()
  val mockedAuthUserAction: AuthenticatedUserAction =
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

  "EntryController#index" should {
    "be OK" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      when(mockedEntryRepository.getEntries) thenReturn Future {
        Seq((1L, "dummy-title"), (2L, "dummy-title"))
      }
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedAuthUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result = controller.index.apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }

  "EntryController#edit" should {
    "be OK if the session has started" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {

      val action: AuthenticatedUserAction =
        app.injector.instanceOf[AuthenticatedUserAction]
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            action)
      val session: (String, String) = (Constant.SESSION_USER_KEY, 1.toString)
      val request: RequestHeader =
        FakeRequest().withSession(session).withCSRFToken
      val result = controller.edit.apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }

  "EntryController#edit" should {
    "be FORBIDDEN if the session is empty" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {

      val action: AuthenticatedUserAction =
        app.injector.instanceOf[AuthenticatedUserAction]
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            action)
      val request: RequestHeader =
        FakeRequest().withSession().withCSRFToken
      val result = controller.edit.apply(request)

      status(result) mustBe FORBIDDEN
    }
  }

  "EntryController#archive" should {
    "be SEE_OTHER" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val userId = 1
      val title = "dummy-title"
      val content = "dummy-content"
      val url = Option("/tmp/media-sample-scala/test.png")
      val size = Option(46917L)

      val action: AuthenticatedUserAction =
        app.injector.instanceOf[AuthenticatedUserAction]
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      val mockedEntryService: EntryService = mock[EntryService]
      when(mockedEntryService.create(userId, title, content, url, size)) thenReturn Future {
        1L
      }
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            action)

      val form = Map(("title", Seq(title)), ("content", Seq(content)))
      val file = new java.io.File("./Public/images/test.png")
      val part: FilePart[File] = FilePart[File](key = "img",
                                                filename = "blank.png",
                                                contentType =
                                                  Option("image/png"),
                                                ref = file)
      val multiPart: MultipartFormData[File] =
        MultipartFormData[File](dataParts = form,
                                files = Seq(part),
                                badParts = Nil)

      val session: (String, String) = (Constant.SESSION_USER_KEY, 1.toString)
      val request: RequestHeader =
        FakeRequest()
          .withSession(session)
          .withBody(multiPart)
          .withCSRFToken
      val result = controller.archive.apply(request)
      val expectedFlash = Flash(Map("success" -> "entry.created"))

      status(result) mustBe SEE_OTHER
      flash(result) must equal(expectedFlash)
    }
  }

  "EntryController#list" should {
    "be OK" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      when(mockedEntryRepository.getEntries) thenReturn Future {
        Seq((1L, "dummy-title"), (2L, "dummy-title"))
      }
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedAuthUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result = controller.list.apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }

  "EntryController#entry" should {
    "be OK if specify registered entry id" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val entryId = 1
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      when(mockedEntryRepository.getEntry(entryId)) thenReturn Future {
        Option(Entry(entryId, 1, "dummy-title", "dummy-content"))
      }
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedAuthUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result = controller.entry(entryId).apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }

  "EntryController#entry" should {
    "be Forbidden if specify not registered entry id" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val entryId: Port = -1
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      when(mockedEntryRepository.getEntry(entryId)) thenReturn Future {
        None
      }
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedAuthUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result = controller.entry(entryId).apply(request)

      status(result) equals Forbidden
    }
  }

}
