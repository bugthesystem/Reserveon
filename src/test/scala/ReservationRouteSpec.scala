import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken }
import akka.http.scaladsl.model.{ HttpEntity, MediaTypes }
import akka.util.ByteString
import io.circe.generic.auto._
import io.circe.syntax._
import models._
import org.scalatest.BeforeAndAfterEach
import utils.CacheConstants

import scala.concurrent.Future

import models.ModelCodecs.reservationCounter._

class ReservationRouteSpec extends SpecBase with TestFixture
    with CacheConstants with utils.Messages.Reservation with BeforeAndAfterEach {

  def actorRefFactory = system

  override def beforeEach(): Unit = {
    oAuthAccessTokensService.findByAccessToken("valid token") returns Future(Some(testToken))
    accountsService.findAccountById(1) returns Future(Some(testUser))
    oAuthClientsService.findClientByClientId(1) returns Future(Some(testClient))
  }

  val testImdbId = "testImdbId"
  val testScreenId = "testScreenId"
  val reservationToCreate = ReservationCreate(imdbId = testImdbId, screenId = testScreenId)

  "Reservation Routes (with valid credentials)" should {

    "create reservation" in {
      val result = ReservationCreateResult(message = RESERVATION_CREATED, success = true)

      reservationService.makeReservation(reservationToCreate) returns Future(result)

      val cacheKey = RESERVATION_TRACK_KEY_TPL.format(testImdbId, testScreenId)
      cacheService.existsInCache(cacheKey) returns Future(true)

      val state = ReservationCounter(availableSeats = 1, reservedSeats = 0)
      cacheService.getFromCache[ReservationCounter](cacheKey)(decodeReservationCounter) returns Future(Some(state))

      //INFO:
      // I expected that I can check argument as following but, I could not see solution like this
      // it[ReservationCounter].is(rs=> rs.reservedSeats == 1)

      val expectedReservationState = ReservationCounter(state.availableSeats, state.reservedSeats + 1)
      cacheService.addToCache[ReservationCounter](cacheKey, expectedReservationState)(encodeReservationCounter) returns Future(true)

      val reservationCreateRequest = ByteString(
        s"""{
           |"imdbId": "$testImdbId",
           |"screenId": "$testScreenId"
           |}""".stripMargin
      )

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      val httpEntity = HttpEntity(MediaTypes.`application/json`, reservationCreateRequest)
      Post("/v1/reservation", httpEntity).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK

        responseAs[String] shouldEqual RESERVATION_CREATED
      }
    }

    "fail create reservation when there is no available seat" in {
      val result = ReservationCreateResult(message = RESERVATION_CREATE_NO_AVAILABLE_SEAT, success = false)

      reservationService.makeReservation(reservationToCreate) returns Future(result)

      val cacheKey = RESERVATION_TRACK_KEY_TPL.format(testImdbId, testScreenId)
      cacheService.existsInCache(cacheKey) returns Future(true)

      val state = ReservationCounter(availableSeats = 1, reservedSeats = 1)
      cacheService.getFromCache[ReservationCounter](cacheKey)(decodeReservationCounter) returns Future(Some(state))

      val reservationCreateRequest = ByteString(
        s"""{
           |"imdbId": "$testImdbId",
           |"screenId": "$testScreenId"
           |}""".stripMargin
      )

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      val httpEntity = HttpEntity(MediaTypes.`application/json`, reservationCreateRequest)

      Post("/v1/reservation", httpEntity).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual BadRequest

        responseAs[String] shouldEqual RESERVATION_CREATE_NO_AVAILABLE_SEAT
      }
    }

    "fail create reservation when there is no valid movie entry" in {
      val result = ReservationCreateResult(message = RESERVATION_CREATE_NO_AVAILABLE_MOVIE, success = false)

      reservationService.makeReservation(reservationToCreate) returns Future(result)

      val cacheKey = RESERVATION_TRACK_KEY_TPL.format(testImdbId, testScreenId)
      cacheService.existsInCache(cacheKey) returns Future(false)

      val reservationCreateRequest = ByteString(
        s"""{
           |"imdbId": "$testImdbId",
           |"screenId": "$testScreenId"
           |}""".stripMargin
      )

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      val httpEntity = HttpEntity(MediaTypes.`application/json`, reservationCreateRequest)

      Post("/v1/reservation", httpEntity).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual BadRequest

        responseAs[String] shouldEqual RESERVATION_CREATE_NO_AVAILABLE_MOVIE
      }
    }

    "fail create reservation when there is no valid movie (cache expire right after exists check) entry" in {
      val result = ReservationCreateResult(message = RESERVATION_CREATE_NO_AVAILABLE_MOVIE, success = false)

      reservationService.makeReservation(reservationToCreate) returns Future(result)

      val cacheKey = RESERVATION_TRACK_KEY_TPL.format(testImdbId, testScreenId)
      cacheService.existsInCache(cacheKey) returns Future(true)

      cacheService.getFromCache[ReservationCounter](cacheKey)(decodeReservationCounter) returns Future(None)

      val reservationCreateRequest = ByteString(
        s"""{
           |"imdbId": "$testImdbId",
           |"screenId": "$testScreenId"
           |}""".stripMargin
      )

      val authHeader = Authorization(OAuth2BearerToken("valid token"))
      val httpEntity = HttpEntity(MediaTypes.`application/json`, reservationCreateRequest)

      Post("/v1/reservation", httpEntity).addHeader(authHeader) ~> httpService.routes ~> check {
        handled shouldEqual true
        status shouldEqual BadRequest

        responseAs[String] shouldEqual RESERVATION_CREATE_NO_AVAILABLE_MOVIE
      }
    }

    "return movie reservation details" in {
      val cacheKey = RESERVATION_TRACK_KEY_TPL.format(testImdbId, testScreenId)

      val movie = Movie(Option(1), testImdbId, "Test Movie Title", 10, testScreenId, createdAt)
      val state = ReservationCounter(availableSeats = movie.availableSeats, reservedSeats = 3)
      val detail = MovieReservationDetail(testImdbId, movie.movieTitle,
        testScreenId, state.availableSeats, state.reservedSeats)

      moviesService.findMovieByImdbId(testImdbId) returns Future(Some(movie))
      cacheService.getFromCache[ReservationCounter](cacheKey)(decodeReservationCounter) returns Future(Some(state))
      reservationService.getReservationDetail(testImdbId, testScreenId) returns Future(Some(detail))

      val authHeader = Authorization(OAuth2BearerToken("valid token"))

      Get(s"/v1/reservation/?imdbId=$testImdbId&screenId=$testScreenId", HttpEntity("application/json"))
        .addHeader(authHeader) ~> httpService.routes ~> check {
          handled shouldEqual true
          status shouldEqual OK

          val response = responseAs[MovieReservationDetail]
          response.availableSeats shouldEqual movie.availableSeats
          response.reservedSeats shouldEqual state.reservedSeats
          response.imdbId shouldEqual testImdbId
          response.screenId shouldEqual testScreenId
          response.movieTitle shouldEqual movie.movieTitle
        }
    }
  }
}
