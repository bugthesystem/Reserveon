package services

import models.db.MovieEntityTable
import models.{Movie, ReservationCounter}
import utils.CacheConstants

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

trait MoviesService {

  def getMovies(): Future[Seq[Movie]]

  def findMovieById(id: Long): Future[Option[Movie]]

  def findMovieByImdbId(imdbId: String): Future[Option[Movie]]

  def createMovie(movie: Movie): Future[Option[Movie]]

  def deleteMovie(id: Long): Future[Int]
}

class MoviesServiceImpl(
                         val databaseService: DatabaseService,
                         val cacheService: CachingService
                       )(implicit executionContext: ExecutionContext)
  extends MovieEntityTable with MoviesService with CacheConstants {

  import cacheService._
  import databaseService._
  import databaseService.driver.api._
  import models.ModelCodecs.reservationCounter._

  override def getMovies(): Future[Seq[Movie]] = db.run(movies.result)

  override def findMovieById(id: Long): Future[Option[Movie]] = db.run(movies.filter(_.id === id).result).map(_.headOption)

  override def findMovieByImdbId(imdbId: String): Future[Option[Movie]] = db.run(movies.filter(_.imdbId === imdbId).result).map(_.headOption)

  override def createMovie(movie: Movie): Future[Option[Movie]] = {
    //Create movie entry and reservation cache entry
    val key = RESERVATION_TRACK_KEY_TPL.format(movie.imdbId, movie.screenId)
    val reservation = ReservationCounter(availableSeats = movie.availableSeats, reservedSeats = 0)

    for {

      created <- db.run(movies returning movies += movie)
      _ <- addToCache[ReservationCounter](key, reservation)(encodeReservationCounter)
    } yield {
      if (created.id.isEmpty) None else Some(created)
    }
  }

  override def deleteMovie(id: Long): Future[Int] = db.run(movies.filter(_.id === id).delete)
}

