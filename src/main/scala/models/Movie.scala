package models

import java.sql.Timestamp

import org.joda.time.DateTime

case class MovieCreate(
    imdbId: String,
    movieTitle: String,
    availableSeats: Int,
    screenId: String
) {
  def toMovie(): Movie = {
    val createdAt = new Timestamp(DateTime.now().getMillis)
    Movie(Option(0), imdbId, movieTitle, availableSeats, screenId, createdAt)
  }
}

case class Movie(
    id: Option[Long] = None,
    imdbId: String,
    movieTitle: String,
    availableSeats: Int,
    screenId: String,
    createdAt: Timestamp
) {
  require(!imdbId.isEmpty, "imdbId.empty")
  require(!movieTitle.isEmpty, "movieTitle.empty")
  require(availableSeats > 0, "availableSeats.empty")
  require(!screenId.isEmpty, "screenId.empty")
}

class MovieDetails(
  id: Option[Long] = None,
  imdbId: String,
  movieTitle: String,
  availableSeats: Int,
  reservedSeats: Int,
  screenId: String,
  createdAt: Timestamp
)
    extends Movie(id, imdbId, movieTitle, availableSeats, screenId, createdAt)