package view.controllers

import controllers.{Assets, WebJarAssets}
import play.api.Play
import play.api.Play.current

/**
 * See http://www.jamesward.com/2014/03/20/webjars-now-on-the-jsdelivr-cdn
 */
object CdnWebJarAssets extends WebJarAssets(Assets) {

  def getUrl(file: String) = {
    val maybeContentUrl = Play.configuration.getString("contentUrl")

    maybeContentUrl.map { contentUrl =>
        contentUrl + view.controllers.routes.CdnWebJarAssets.at(file).url
    } getOrElse view.controllers.routes.CdnWebJarAssets.at(file).url
  }

}
