package http

import akka.http.scaladsl.server.Directives._
import handlers.OAuth2DataHandler
import http.routes.{ MoviesRoute, OAuthRoute, ReservationRoute }
import services._
import utils.CorsSupport

import scala.concurrent.ExecutionContext

class HttpService(
  moviesService: MoviesService,
  oAuthClientsService: OAuthClientsService,
  oAuthAccessTokensService: OAuthAccessTokensService,
  accountsService: AccountsService,
  cachingService: CachingService,
  reservationService: ReservationService
)(implicit executionContext: ExecutionContext)
    extends CorsSupport {

  val oAuth2DataHandler = new OAuth2DataHandler(oAuthClientsService, oAuthAccessTokensService, accountsService)
  val oauthRouter = new OAuthRoute(oAuth2DataHandler)
  val moviesRouter = new MoviesRoute(oAuth2DataHandler, moviesService)
  val reservationRouter = new ReservationRoute(oAuth2DataHandler, reservationService, cachingService)

  val routes =
    corsHandler {
      oauthRouter.route
    } ~
      pathPrefix("v1") {
        corsHandler {

          moviesRouter.route ~
            reservationRouter.route
        }
      }
}