package view.controllers

import play.api.Play.current
import play.api.Routes
import play.api.cache.Cached
import play.api.mvc._

/** Application controller, handles authentication */
object Application extends Controller {

  def checkPreFlight(path: String) = Action {
    Ok.withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> current.configuration.getString("application.allow.origin").get,
      ACCESS_CONTROL_ALLOW_METHODS -> current.configuration.getString("application.allow.methods").get,
      ACCESS_CONTROL_ALLOW_HEADERS -> current.configuration.getString("application.allow.headers").get
    )
  }

  /**
   * Retrieves all routes via reflection.
   * http://stackoverflow.com/questions/12012703/less-verbose-way-of-generating-play-2s-javascript-router
   * @todo If you have controllers in multiple packages, you need to add each package here.
   */
  val routeCache = {
    val jsRoutesClass = classOf[api.controllers.routes.javascript]
    val controllers = jsRoutesClass.getFields.map(_.get(null))
    controllers.flatMap { controller =>
      controller.getClass.getDeclaredMethods.map { action =>
        action.invoke(controller).asInstanceOf[play.core.Router.JavascriptReverseRoute]
      }
    }
  }
  val routeCache1 = {
    val jsRoutesClass = classOf[view.controllers.routes.javascript]
    val controllers = jsRoutesClass.getFields.map(_.get(null))
    controllers.flatMap { controller =>
      controller.getClass.getDeclaredMethods.map { action =>
        action.invoke(controller).asInstanceOf[play.core.Router.JavascriptReverseRoute]
      }
    }
  }

  /**
   * Returns the JavaScript router that the client can use for "type-safe" routes.
   * Uses browser caching; set duration (in seconds) according to your release cycle.
   * @param varName The name of the global variable, defaults to `jsRoutes`
   */
  def jsRoutes(varName: String = "jsRoutes") = Cached(_ => "jsRoutes", duration = 86400) {
    Action { implicit request =>
      Ok(
        Routes.javascriptRouter(varName)(routeCache ++ routeCache1: _*).concat(
          "var meekbiz = meekbiz || {};meekbiz.settings = {};" +
          "meekbiz.settings.backendHost = '" + current.configuration.getString("application.backend.url.prefix").get + "';" +
          "meekbiz.settings.s3host = '" + current.configuration.getString("aws.s3.host").get + "';"
        )
      ).as(JAVASCRIPT)
    }
  }
}
