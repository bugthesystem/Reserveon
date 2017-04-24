package services

import models.{ ReservationCreate, ReservationCreateResult, MovieReservationDetail }
import utils.CacheConstants

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.postfixOps

trait ReservationService {
  def createReservation(reservation: ReservationCreate): Future[ReservationCreateResult]

  def getReservationDetail(imdbId: String, screenId: String): Future[Option[MovieReservationDetail]]
}

class ReservationServiceImpl(
  val moviesService: MoviesService,
  val cacheService: CachingService
)(implicit executionContext: ExecutionContext)
    extends ReservationService with CacheConstants with utils.Messages.Reservation {

  import cacheService._
  import moviesService._

  override def createReservation(reservation: ReservationCreate): Future[ReservationCreateResult] = {

    val key = RESERVATION_TRACK_KEY_TPL.format(reservation.imdbId, reservation.screenId)
    val exists = Await.result(existsInCache(key), 1 seconds)
    if (!exists) {
      Future {
        ReservationCreateResult(message = RESERVATION_CREATE_NO_AVAILABLE_MOVIE, success = false)
      }
    } else {
      getReservationStateFromCache(key).map {
        case Some(cachedReservation) => {
          if (cachedReservation.availableSeats > cachedReservation.reservedSeats) {
            cachedReservation.reserve(1)
            ReservationCreateResult(
              message = RESERVATION_CREATED,
              success = Await.result(addReservationToCache(key, cachedReservation), 1 seconds)
            )
          } else {
            ReservationCreateResult(message = RESERVATION_CREATE_NO_AVAILABLE_SEAT, success = false)
          }
        }
        case None => ReservationCreateResult(
          message = RESERVATION_CREATE_NO_AVAILABLE_MOVIE,
          success = false
        )
      }
    }
  }

  override def getReservationDetail(imdbId: String, screenId: String): Future[Option[MovieReservationDetail]] = {
    findMovieByImdbId(imdbId).map {
      case Some(movie) => {
        val key = RESERVATION_TRACK_KEY_TPL.format(imdbId, screenId)

        val cachedReservationOption = Await.result(getReservationStateFromCache(key), 1 seconds)

        cachedReservationOption match {
          case Some(c) => {
            Some(MovieReservationDetail(
              imdbId = imdbId,
              movieTitle = movie.movieTitle,
              screenId = screenId,
              availableSeats = c.availableSeats,
              reservedSeats = c.reservedSeats
            ))
          }
          case None => None
        }
      }
      case None => None
    }
  }
}
