package models

import io.circe._
import io.circe.syntax._
import utils.CirceCommonCodecs

object ModelCodecs extends CirceCommonCodecs {

  object movie {
    implicit val decodeMovie: Decoder[Movie] =
      Decoder.forProduct6(
        "id",
        "imdbId",
        "movieTitle",
        "availableSeats",
        "screenId",
        "createdAt"
      )(Movie.apply)

    implicit val encodeMovie: Encoder[Movie] =
      Encoder.forProduct6(
        "id",
        "imdbId",
        "movieTitle",
        "availableSeats",
        "screenId",
        "createdAt"
      )(m =>
          (m.id, m.imdbId, m.movieTitle, m.availableSeats, m.screenId, m.createdAt))
  }

  object reservationCounter {
    implicit val decodeReservationCounter: Decoder[ReservationCounter] =
      Decoder.forProduct2("availableSeats", "reservedSeats")(ReservationCounter.apply)

    implicit val encodeReservationCounter: Encoder[ReservationCounter] =
      Encoder.forProduct2("availableSeats", "reservedSeats")(rc =>
        (rc.availableSeats, rc.reservedSeats))
  }

}
