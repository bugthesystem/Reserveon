
package http.routes

import akka.http.scaladsl.server.{ Directives, Route }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import handlers.OAuth2DataHandler
import http.auth.OAuth2RouteProvider
import io.circe.generic.auto._
import io.circe.syntax._
import models.{ Account, ReservationCreate }
import services.{ CachingService, ReservationService }
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.ExecutionContext
import scalaoauth2.provider.AuthInfo

class ReservationRoute(
  override val oauth2DataHandler: OAuth2DataHandler,
  reservationService: ReservationService,
  cachingService: CachingService
)(implicit executionContext: ExecutionContext)
    extends Directives
    with OAuth2RouteProvider[Account]
    with FailFastCirceSupport {

  import reservationService._

  val route: Route = pathPrefix("reservation") {
    pathEndOrSingleSlash {
      post {
        authenticateOAuth2Async[AuthInfo[Account]]("realm", oauth2Authenticator) {
          _ =>
            entity(as[ReservationCreate]) { reservation =>
              onSuccess(createReservation(reservation)) {
                result =>
                  {
                    if (result.success) {
                      complete(result.message)
                    } else {
                      complete(BadRequest, result.message)
                    }
                  }
              }
            }
        }
      } ~ parameters('imdbId.as[String], 'screenId.as[String]) { (imdbId, screenId) =>
        {
          authenticateOAuth2Async[AuthInfo[Account]]("realm", oauth2Authenticator) {
            _ =>
              onSuccess(getReservationDetail(imdbId, screenId)) {
                case Some(detail) => complete(detail.asJson)
                case None => complete(BadRequest)
              }

          }
        }
      }
    }
  }
}
