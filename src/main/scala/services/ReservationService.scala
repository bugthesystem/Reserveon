package services

import models.{ MovieReservationDetail, ReservationCounter, ReservationCreate, ReservationCreateResult }
import utils.CacheConstants

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

trait ReservationService {
  def makeReservation(reservation: ReservationCreate): Future[ReservationCreateResult]

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

  //TODO: Implement distributed lock mechanism (redis, zookeeper etc..)
  //INFO: https://redis.io/topics/distlock
  def makeReservation(reservationOption: Option[ReservationCounter]): Future[(String, Boolean)] = {
    reservationOption match {
      case Some(value) => {

        //ACQUIRE LOCK
        if (value.availableSeats > value.reservedSeats) {
          value.makeReservation()
          Future {
            (RESERVATION_CREATED, true)
          }
        } else {
          Future {
            (RESERVATION_CREATE_NO_AVAILABLE_SEAT, false)
          }
        }
        //RELEASE LOCK

      }
      case None => Future {
        (RESERVATION_CREATE_NO_AVAILABLE_MOVIE, false)
      }
    }
  }

  def processReservation(key: String): Future[ReservationCreateResult] = {
    for {
      cachedReservationOpt <- getFromCache[ReservationCounter](key)(decodeReservationCounter)
      (message, ok) <- makeReservation(cachedReservationOpt)
      added <- addToCache[ReservationCounter](key, cachedReservationOpt.get)(encodeReservationCounter)
    } yield ReservationCreateResult(message = message, success = added & ok)
  }

  override def makeReservation(reservation: ReservationCreate): Future[ReservationCreateResult] = {

    val key = RESERVATION_TRACK_KEY_TPL.format(reservation.imdbId, reservation.screenId)

    val message = Future {
      ReservationCreateResult(message = RESERVATION_CREATE_NO_AVAILABLE_MOVIE, success = false)
    }

    for {
      exists: Boolean <- existsInCache(key)
      result <- if (exists) message else processReservation(key)
    } yield result
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
