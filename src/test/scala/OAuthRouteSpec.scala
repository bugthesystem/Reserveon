import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken }
import akka.http.scaladsl.model.{ FormData, HttpEntity }
import http.HttpService
import http.auth.OAuth2RouteProvider.TokenResponse
import models.Movie

import scala.concurrent.Future

class OAuthRouteSpec extends SpecBase with TestFixture {

  def actorRefFactory = system

  "OAuth Routes" should {
    "return unauthorized when trying to get a token without any credentials" in {
      Post("/oauth/access_token") ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual Unauthorized

      }
    }

    "return Ok and a token when trying to get a token with valid credentials" in {
      oAuthClientsService.validate("test_client_id", "test_client_secret", "client_credentials") returns (Future(true))
      oAuthClientsService.findClientCredentials("test_client_id", "test_client_secret") returns Future(Some(testUser))
      oAuthAccessTokensService.findByAuthorized(testUser, "test_client_id") returns Future(Some(testToken))

      val testFormData = FormData(
        "client_id" -> "test_client_id",
        "client_secret" -> "test_client_secret",
        "grant_type" -> "client_credentials"
      )
      Post("/oauth/access_token", testFormData) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        val response = responseAs[TokenResponse]
        response.access_token shouldEqual "valid token"
        response.refresh_token shouldEqual "refresh token"
        response.token_type shouldEqual "Bearer"
      }

    }

    "return Ok and a token when trying to get a token with valid password" in {

      oAuthClientsService.validate("test_client_id", "test_client_secret", "password") returns (Future(true))
      accountsService.authenticate("testmail@gmail.com", "pass") returns Future(Some(testUser))
      oAuthAccessTokensService.findByAuthorized(testUser, "test_client_id") returns Future(Some(testToken))

      val testFormData = FormData(
        "client_id" -> "test_client_id",
        "client_secret" -> "test_client_secret",
        "username" -> "testmail@gmail.com",
        "password" -> "pass",
        "grant_type" -> "password"
      )
      Post("/oauth/access_token", testFormData) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        val response = responseAs[TokenResponse]
        response.access_token shouldEqual "valid token"
        response.refresh_token shouldEqual "refresh token"
        response.token_type shouldEqual "Bearer"
      }

    }

    "return new token after refresh" in {

      oAuthClientsService.validate("test_client_id", "test_client_secret", "refresh_token") returns (Future(true))
      oAuthAccessTokensService.findByRefreshToken("refresh token") returns Future(Some(testToken))
      accountsService.findAccountById(1) returns Future(Some(testUser))
      oAuthClientsService.findClientByClientId(1) returns Future(Some(testClient))
      oAuthClientsService.findByClientId("test_client_id") returns Future(Some(testClient))
      oAuthAccessTokensService.refresh(testUser, testClient) returns Future(testToken)

      val testFormData = FormData("client_id" -> "test_client_id", "client_secret" -> "test_client_secret",
        "refresh_token" -> "refresh token", "grant_type" -> "refresh_token")
      Post("/oauth/access_token", testFormData) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        val response = responseAs[TokenResponse]
        response.access_token shouldEqual "valid token"
        response.refresh_token shouldEqual "refresh token"
        response.token_type shouldEqual "Bearer"
      }
    }

    "don't handle when trying to access authorized resources without token" in {
      oAuthAccessTokensService.findByAccessToken("") returns Future(None)

      Get("/movies") ~> httpService.routes ~> check {
        handled shouldEqual false
      }
    }

    "return unauthorized when trying to access authorized resources without token" in {
      oAuthAccessTokensService.findByAccessToken("invalid_token") returns Future(None)

      Get("/v1/movies").addHeader(Authorization(OAuth2BearerToken("invalid_token"))) ~> httpService.routes ~> check {
        handled shouldEqual false
      }
    }

    "return authorized when trying to access resources with a valid token" in {

      oAuthAccessTokensService.findByAccessToken("valid token") returns Future(Some(testToken))

      accountsService.findAccountById(1) returns Future(Some(testUser))
      oAuthClientsService.findClientByClientId(1) returns Future(Some(testClient))

      moviesService.getMovies() returns Future(Seq[Movie]())

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      Get("/v1/movies", HttpEntity("application/json")).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

  }
}
