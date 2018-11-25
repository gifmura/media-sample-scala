package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class AuthenticatedAccountController @Inject()(
                                             cc: ControllerComponents,
                                             authenticatedUserAction: AuthenticatedAccountAction
                                           ) extends AbstractController(cc) {

  def logout = authenticatedUserAction { implicit request: Request[AnyContent] =>
    // docs: “withNewSession ‘discards the whole (old) session’”
    Redirect(routes.AccountController.index)
      .flashing("info" -> "You are logged out.")
      .withNewSession
  }

}