package http.routes

import java.sql.Timestamp

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import handlers.OAuth2DataHandler
import http.auth.OAuth2RouteProvider
import io.circe.Decoder.Result
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import models.{Account, MovieCreate}
import services.MoviesService

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scalaoauth2.provider.AuthInfo

class MoviesRoute(override val oauth2DataHandler: OAuth2DataHandler, moviesService: MoviesService)(implicit executionContext: ExecutionContext)
    extends OAuth2RouteProvider[Account]
    with FailFastCirceSupport {

  import moviesService._

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

  val route: Route = pathPrefix("movies") {
    pathEndOrSingleSlash {
      get {
        authenticateOAuth2Async[AuthInfo[Account]]("realm", oauth2Authenticator) {
          _ => complete(getMovies().map(_.asJson))
        }
      } ~
        post {
          authenticateOAuth2Async[AuthInfo[Account]]("realm", oauth2Authenticator) {
            _ =>
              entity(as[MovieCreate]) { movieToCreate =>
                onSuccess(createMovie(movieToCreate.toMovie())) {
                  case Some(movie) => {
                    extractRequestContext { requestContext =>
                      val request = requestContext.request
                      val location = request.uri.copy(path = request.uri.path / movie.id.get.toString)
                      respondWithHeader(Location(location)) {
                        complete(Created, movie.asJson)
                      }
                    }
                  }
                  case None => {
                    //TODO: return meaningful response; error detail,validation detail etc..
                    complete(400)
                  }
                }
              }

          }
        }
    } ~
      pathPrefix(IntNumber) { id =>
        pathEndOrSingleSlash {
          get {
            authenticateOAuth2Async[AuthInfo[Account]]("realm", oauth2Authenticator) {
              _ =>
                onSuccess(findMovieById(id)) {
                  case Some(m) => complete(m.asJson)
                  case None => complete(NotFound)
                }
            }
          } ~
            delete {
              authenticateOAuth2Async[AuthInfo[Account]]("realm", oauth2Authenticator) {
                _ =>
                  onSuccess(deleteMovie(id)) { _ =>
                    complete(NoContent)
                  }
              }
            }
        }
      }
  }
}
