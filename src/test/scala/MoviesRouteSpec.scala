import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken }
import akka.http.scaladsl.model.{ HttpEntity, MediaTypes }
import akka.util.ByteString
import io.circe.generic.auto._
import io.circe.syntax._
import models.Movie
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Future

class MoviesRouteSpec extends SpecBase with TestFixture with BeforeAndAfterEach with utils.Messages.Movies {

  def actorRefFactory = system

  override def beforeEach(): Unit = {
    oAuthAccessTokensService.findByAccessToken("valid token") returns Future(Some(testToken))
    accountsService.findAccountById(1) returns Future(Some(testUser))
    oAuthClientsService.findClientByClientId(1) returns Future(Some(testClient))
  }

  "Movies Routes (with valid credentials)" should {
    "return list of movies " in {
      val response = Seq[Movie](Movie(Option(1), "testImdbId", "testTitle", 10, "testScreenId", createdAt))
      moviesService.getMovies() returns Future(response)

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      Get("/v1/movies", HttpEntity("application/json")).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Seq[Movie]] should equal(response)
      }
    }

    "create movie should present `Location` header" in {
      val movie = Movie(Some(1), "testImdbId", "testTitle", 10, "testScreenId", createdAt)
      moviesService.createMovie(any[Movie]) returns Future(Some(movie))

      val movieCreateRequest = ByteString(
        s"""{
           |"imdbId": "${movie.imdbId}",
           |"movieTitle": "${movie.movieTitle}",
           |"availableSeats": ${movie.availableSeats},
           |"screenId": "${movie.screenId}"
           |}""".stripMargin
      )

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      val httpEntity = HttpEntity(MediaTypes.`application/json`, movieCreateRequest)
      Post("/v1/movies", httpEntity).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        header("Location").get.value() should endWith("/v1/movies/1")
      }
    }

    "create movie should fail when we trying to add duplicate imdbId" in {
      val movie = Movie(Some(1), "testImdbId", "testTitle", 10, "testScreenId", createdAt)
      moviesService.createMovie(any[Movie]) returns Future(None)

      val movieCreateRequest = ByteString(
        s"""{
           |"imdbId": "${movie.imdbId}",
           |"movieTitle": "${movie.movieTitle}",
           |"availableSeats": ${movie.availableSeats},
           |"screenId": "${movie.screenId}"
           |}""".stripMargin
      )

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      val httpEntity = HttpEntity(MediaTypes.`application/json`, movieCreateRequest)
      Post("/v1/movies", httpEntity).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual BadRequest
        responseAs[String] shouldEqual SOMETHING_WRONG_IN_CLIENT_REQUEST
      }
    }

    "return movie by id" in {
      val testMovieId = 1
      val response = Movie(Option(testMovieId), "testImdbId", "testTitle", 10, "testScreenId", createdAt)
      moviesService.findMovieById(testMovieId) returns Future(Some(response))

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      Get(s"/v1/movies/$testMovieId", HttpEntity("application/json")).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Movie] should equal(response)
      }
    }

    "delete movie by id" in {
      val testMovieId = 1
      moviesService.deleteMovie(testMovieId) returns Future(1)

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      Delete(s"/v1/movies/$testMovieId", HttpEntity("application/json")).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual NoContent
      }
    }
  }
}