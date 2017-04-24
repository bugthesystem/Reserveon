package models

case class ReservationCreate(imdbId: String, screenId: String) {
  require(!imdbId.isEmpty, "imdbId.empty")
  require(!screenId.isEmpty, "screenId.empty")
}

case class ReservationCounter(availableSeats: Int, var reservedSeats: Int) {
  def reserve(count: Int): Unit = {
    reservedSeats += count
  }
}

case class MovieReservationDetail(
  imdbId: String,
  movieTitle: String,
  screenId: String,
  availableSeats: Int,
  var reservedSeats: Int
)

case class ReservationCreateResult(message: String, success: Boolean)