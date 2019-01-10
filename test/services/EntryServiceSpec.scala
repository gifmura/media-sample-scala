package services

import java.io.File

import jp.t2v.lab.play2.pager.{OrderType, Pager, Sorter}
import models._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.{Configuration, Mode}
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test.{Helpers, StubBodyParserFactory}
import services.amazon.S3Service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EntryServiceSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory {

  val mockedEntry: EntryRepository = mock[EntryRepository]

  val mockedImage: ImageRepository = mock[ImageRepository]

  val mockedS3Service: S3Service = mock[S3Service]

  val mockedConfig: Configuration = mock[Configuration]

  val cc = stubMessagesControllerComponents()

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

  "EntryService#findAll" should {
    "return Future[Entry] if a uri of image is not specified" in {
      val sorter = Sorter[Entry]("entryId", OrderType.Ascending)
      val pager =
        Pager[Entry](1, 1, sorter, null)

      when(mockedEntry.countAll) thenReturn Future { 1 }

      when(mockedEntry.findAll(pager.allSorters, pager.limit, pager.offset)) thenReturn Future {
        Seq(Entry(1, 1, "title", "content"))
      }

      val service =
        new EntryService(dbConfProvider,
                         mockedEntry,
                         mockedImage,
                         cc,
                         mockedConfig,
                         mockedS3Service)

      val result = service.findAll(pager)

      result mustBe a[Future[_]]
    }
  }

  "EntryService#create" should {
    "return Future[Entry] if a uri of image is not specified" in {

      val id = 1
      val user_id = 1
      val title = "dummy-title"
      val content = "dummy-content"

      when(mockedEntry.create(user_id, title, content)) thenReturn Future {
        new Entry(id, user_id, title, content)
      }

      when(mockedConfig.get[Boolean]("aws.s3.isEnabled")) thenReturn false

      val service =
        new EntryService(dbConfProvider,
                         mockedEntry,
                         mockedImage,
                         cc,
                         mockedConfig,
                         mockedS3Service)

      val key = "img"
      val filename = "test.png"
      val contentType = Option("image/png")
      val temp = File.createTempFile("pattern", ".suffix")

      val filePart = Option(FilePart[File](key, filename, contentType, temp))

      val result = service.createEntry(user_id, title, content, filePart)

      result mustBe a[Future[_]]
    }
  }

}
