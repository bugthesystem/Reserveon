package services

import models.{ MovieReservationDetail, ReservationCounter, ReservationCreate, ReservationCreateResult }
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
  import models.ModelCodecs.reservationCounter._
  import moviesService._

  override def createReservation(reservation: ReservationCreate): Future[ReservationCreateResult] = {

    val key = RESERVATION_TRACK_KEY_TPL.format(reservation.imdbId, reservation.screenId)
    val exists = Await.result(existsInCache(key), 1 seconds)
    if (!exists) {
      Future {
        ReservationCreateResult(message = RESERVATION_CREATE_NO_AVAILABLE_MOVIE, success = false)
      }
    } else {
      getFromCache[ReservationCounter](key)(decodeReservationCounter).map {
        case Some(cachedReservation) => {
          if (cachedReservation.availableSeats > cachedReservation.reservedSeats) {
            cachedReservation.makeReservation()
            ReservationCreateResult(
              message = RESERVATION_CREATED,
              success = Await.result(addToCache[ReservationCounter](key, cachedReservation)(encodeReservationCounter), 1 seconds)
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
    //INFO: Implement caching to hit to it first
    val key = RESERVATION_TRACK_KEY_TPL.format(imdbId, screenId)

    for {
      movieOption <- findMovieByImdbId(imdbId)
      cachedReservationOption <- getFromCache[ReservationCounter](key)(decodeReservationCounter)
    } yield {
      movieOption match {
        case Some(movie) => {
          cachedReservationOption match {
            case Some(cachedReservation) => {
              Some(MovieReservationDetail(
                imdbId = imdbId,
                movieTitle = movie.movieTitle,
                screenId = screenId,
                availableSeats = cachedReservation.availableSeats,
                reservedSeats = cachedReservation.reservedSeats
              ))
            }
            case None => None
          }
        }
        case None => None
      }
    }
  }
}
