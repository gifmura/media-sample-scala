package controllers

import java.io.FileInputStream

import akka.util.ByteString
import com.amazonaws.services.s3.model.S3ObjectInputStream
import models.ImageRepository
import org.apache.http.client.methods.HttpGet
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test.{WithApplication, _}
import services.amazon.S3Service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageControllerSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory
    with Results {

  val mockedUserRepository: ImageRepository = mock[ImageRepository]

  val mockedS3Service: S3Service = mock[S3Service]

  val mockedConfig: Configuration = mock[Configuration]

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
    "be OK if the entryId was stored in local" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val entryId = 1
      val fileName = "./Public/images/test.png"

      when(mockedUserRepository.getImage(entryId)) thenReturn Future {
        Seq(fileName)
      }

      when(mockedConfig.get[Boolean]("aws.s3.isEnabled")) thenReturn false

      val controller =
        new ImageController(mockedUserRepository,
                            cc,
                            mockedConfig,
                            mockedS3Service)
      val request: RequestHeader = FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.image(entryId).apply(request)

      status(result) mustBe OK
      contentAsString(result).length > 0
    }
  }

  "ImageController#image" should {
    "be OK with a chunked image if the entryId was stored in S3" in new WithApplication(
      GuiceApplicationBuilder()
        .configure("play.http.filters" -> "play.api.http.NoHttpFilters")
        .build()
    ) {
      val entryId = 1
      val fileName = "./Public/images/test.png"

      when(mockedUserRepository.getImage(entryId)) thenReturn Future {
        Seq(fileName)
      }

      when(mockedConfig.get[Boolean]("aws.s3.isEnabled")) thenReturn true

      val in = new FileInputStream(fileName)
      val httpRequest = new HttpGet()
      val collectMetrics = false

      when(mockedS3Service.downloadS3(fileName)) thenReturn new S3ObjectInputStream(
        in,
        httpRequest,
        collectMetrics)

      val controller =
        new ImageController(mockedUserRepository,
                            cc,
                            mockedConfig,
                            mockedS3Service)
      val request: RequestHeader = FakeRequest().withCSRFToken
      val result: Accumulator[ByteString, Result] =
        controller.image(entryId).apply(request)

      status(result) mustBe OK
      contentAsString(result).length > 0
    }
  }

}
