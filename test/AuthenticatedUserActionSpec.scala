import controllers.AuthenticatedUserAction
import models.Constant
import org.mockito.Matchers
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{WithApplication, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticatedUserActionSpec
    extends PlaySpec
    with MockitoSugar
    with StubBodyParserFactory
    with Results {

  "AuthenticatedUserAction#invokeBlock" should {
    "be FORBIDDEN if the session is empty" in new WithApplication {

      val parser: BodyParsers.Default =
        app.injector.instanceOf[BodyParsers.Default]

      val action =
        new AuthenticatedUserAction(parser)
      val request: FakeRequest[
        AnyContentAsEmpty.type
      ] = FakeRequest().withSession()
      val result: Future[Result] = action.invokeBlock(request, Matchers.any())

      status(result) mustBe FORBIDDEN
    }

    "be the specified status if the session has started" in new WithApplication {

      val parser: BodyParsers.Default =
        app.injector.instanceOf[BodyParsers.Default]
      val session: (String, String) = (Constant.SESSION_USER_KEY, 1.toString)
      val action = new AuthenticatedUserAction(parser)
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest().withSession(session)
      val block: Request[_] => Future[Result] =
        _ => Future.successful(new Status(OK))

      val result: Future[Result] = action.invokeBlock(request, block)

      status(result) mustBe OK
    }
  }

}
