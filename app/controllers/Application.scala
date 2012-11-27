package controllers

import play.api.Logger
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.cache.Cache
import models._
import views.html

object Application extends SecureController {
 
  implicit val requiredRight: Option[Right] = Right.USER
  
  /**
  * Login Form
  */
  val loginForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
        case (email, password) => User.authenticate(email, password).isDefined
      }
    ) verifying ("User is not active.", result => result match {
        case (email, password) => {
          val user = User.authenticate(email, password)
          (user.isDefined && user.get.isActive)
        }
      }
    )
  )
  
  /**
  * Login page, no authentication required
  */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /**
  * Handle login form submission.
  */
  def authenticate = Action { implicit request =>
    import play.api.Play.current
    
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      loginInfo => {
        //val returnUrl = request.headers.get(REFERER).getOrElse(routes.Application.index.url)
        Cache.set(loginInfo._1, User.authenticate(loginInfo._1, loginInfo._2).getOrElse(
          Redirect(routes.Application.login).withNewSession.flashing(
            "error" -> "Problem with user account.")
          ), 100)
        Redirect(routes.Application.index).withSession(Security.username -> loginInfo._1)
      }
    )
  }

  /**
  * Logout and clean the session.
  */
  def logout = authenticated[AnyContent] { implicit request =>
    import play.api.Play.current
    import play.api.cache._
    
    request.session.get(Security.username) match {
      case Some(userName) => {
	    current.plugin[EhCachePlugin].map {
	      ehcache => ehcache.cache.remove(userName)
	    }.getOrElse(false)
      }
      case None => {}
    }

    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
  
  def index = Action { implicit request =>
    Ok(html.index("Your new application is ready."))
  }
  
  def everybody = authenticated[AnyContent] { implicit request =>
    Ok(html.everybody("Everybody who is authenticated can access this page"))
  }

  def usersOnly = authorized[AnyContent] { implicit request =>
    Ok(html.usersonly("Only people with the User right can access this page."))
  }

  def adminOnly = authorized[AnyContent] { implicit request =>
    Ok(html.adminonly("Only people with the Admin right can access this page."))
  }(requiredRight = Right.ADMIN) //Override the parent implict on a per method basis
}