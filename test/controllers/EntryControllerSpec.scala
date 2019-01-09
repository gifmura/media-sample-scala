package controllers

import java.io.File

import akka.util.ByteString
import javax.inject.Inject
import jp.t2v.lab.play2.pager.{Pager, SearchResult}
import models.{Entry, EntryRepository}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import services.EntryService
import services.session.SessionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EntryControllerSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory
    with Results {

  val cc: MessagesControllerComponents = stubMessagesControllerComponents()

  val mockedUserAction: UserInfoAction = mock[UserInfoAction]

  val mockedEntryRepository: EntryRepository = mock[EntryRepository]

  val mockedEntryService: EntryService = mock[EntryService]

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
      when(mockedEntryRepository.getEntries) thenReturn Future {
        Seq((1L, "dummy-title"), (2L, "dummy-title"))
      }

      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.index(Pager.default[Entry]).apply(request)

      status(result) mustBe SEE_OTHER
    }
  }

  "EntryController#edit" should {
    "be OK if the session has started" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val dummyUserAction: DummyUserInfoAction =
        app.injector.instanceOf[DummyUserInfoAction]

      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            dummyUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.edit.apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
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

      val key = "img"
      val filename = "test.png"
      val contentType = Option("image/png")
      val file = new File("./Public/images/test.png")

      val filePart = Option(FilePart[File](key, filename, contentType, file))

      when(mockedEntryService.createEntry(userId, title, content, filePart)) thenReturn Future {
        1L
      }

      val dummyUserAction: DummyUserInfoAction =
        app.injector.instanceOf[DummyUserInfoAction]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            dummyUserAction)

      val form: Map[String, Seq[String]] =
        Map(("title", Seq(title)), ("content", Seq(content)))

      val multiPart: MultipartFormData[File] =
        MultipartFormData[File](dataParts = form,
                                files = Seq(filePart.get),
                                badParts = Nil)

      val request: RequestHeader =
        FakeRequest()
          .withBody(multiPart)
          .withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.archive.apply(request)
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
      val entryId = 1
      val userId = 1
      val title = "dummy-title"
      val content = "dummy-content"
      val totalCount = 1
      val pager = Pager.default[Entry]
      val dummySearchResult =
        SearchResult[Entry](pager,
                            Seq(Entry(entryId, userId, title, content)),
                            totalCount)

      when(mockedEntryService.findAll(null)) thenReturn Future {
        dummySearchResult
      }
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.list(null).apply(request)

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
      val userId = 1
      val title = "dummy-title"
      val content = "dummy-content"

      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      when(mockedEntryRepository.getEntry(entryId)) thenReturn Future {
        Option(Entry(entryId, userId, title, content))
      }
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.entry(entryId).apply(request)

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
      val entryId = -1
      val mockedEntryRepository: EntryRepository = mock[EntryRepository]
      when(mockedEntryRepository.getEntry(entryId)) thenReturn Future {
        None
      }
      val mockedEntryService: EntryService = mock[EntryService]
      val controller =
        new EntryController(mockedEntryRepository,
                            mockedEntryService,
                            cc,
                            mockedUserAction)

      val request: RequestHeader =
        FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.entry(entryId).apply(request)

      status(result) equals Forbidden
    }
  }

}

class DummyUserInfoAction @Inject()(
    sessionService: SessionService,
    factory: UserInfoCookieBakerFactory,
    playBodyParsers: PlayBodyParsers,
    messagesApi: MessagesApi
)(override implicit val executionContext: ExecutionContext)
    extends UserInfoAction(sessionService,
                           factory,
                           playBodyParsers,
                           messagesApi)(executionContext)
    with Results {

  override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  override def invokeBlock[A](
      request: Request[A],
      block: UserRequest[A] => Future[Result]): Future[Result] = {
    block(new UserRequest[A](request, Option(UserInfo("1")), messagesApi))
  }
}
