package controllers

import play.api.Logger
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.cache.Cache
import models.User
import models._

/**
 * Largely (almost entirely) based on Brian Smith's implementation
 * https://gist.github.com/3160915
 */

/**
* Provide security features
*/
trait SecureController extends Controller {
  import models._
  
  implicit def user(implicit request: RequestHeader): Option[User] = {
    import play.api.Play.current
    
    request.session.get(Security.username) match {
      case Some(userName) => {
        Cache.getOrElse(userName) {
          models.User.findByEmail(userName) match {
            case Some(u) => {
              Cache.set(userName, u, 100) //Adjust cache timeout to your projects needs
              Some(u)
            }
            case _ => {
              None
            }
          }
        }
      }
      case _ => {
        None
      } 
    }
  }    

  /**
  * A utility function that produces different actions by applying either of two request handling
  * functions depending on whether or not a user is authenticated
  *
  * @param allowed the function used where an authenticated user is found
  * @param notAllowed the function used where no user is found
  * @param parser a BodyParser
  *
  */  
  case class AuthenticatedRequest[A](val user: models.User, val request: Request[A]) extends WrappedRequest(request)
    
  def authenticated[A](allowed: AuthenticatedRequest[A] => Result,
                       notAllowed: Request[A] => Result = ((r : Request[A]) => Results.Redirect(routes.Application.login).flashing("error" -> "Please log in before continuing.")))
                      (implicit parser: BodyParser[A] = parse.anyContent) = {
      Action(parser) { implicit request =>
        user.map { user =>
          allowed(AuthenticatedRequest(user, request))
        }.getOrElse(notAllowed(request))
      }
    }
  
  /**
  * A utility function that produces different actions by applying either of two request handling
  * functions depending on whether or not a user is authenticated and has the required rights
  *
  * @param allowed the function used where an authenticated user is found
  * @param requiredRight the right required for this to be allowed
  * @param notAllowed the function used where no user is found
  * @param parser a BodyParser
  *
  */
  def authorized[A](allowed: AuthenticatedRequest[A] => Result,
                    notAllowed: Request[A] => Result = ((r : Request[A]) => Unauthorized(views.html.errors.unauthorized.render(r))))
                   (implicit parser: BodyParser[A] = parse.anyContent, requiredRight: Option[Right] = None) = {
    authenticated ({ implicit request: AuthenticatedRequest[A] =>
      requiredRight match {
        case None => allowed(request)
        case Some(right) => if (request.user.hasRole(right)) allowed(request) else notAllowed(request)
      }
    })(parser)
  }  
 
}