package utils

object Messages {

  trait Reservation {
    val RESERVATION_CREATED = "Reservation created"
    val RESERVATION_CREATE_NO_AVAILABLE_SEAT = "There is no available seat for this movie!"
    val RESERVATION_CREATE_NO_AVAILABLE_MOVIE = "Requested movie does not exists!"
  }

  trait Movies {
    //from RFC 7231
    val SOMETHING_WRONG_IN_CLIENT_REQUEST = "An error occurred while processing your request, please check your request payload"
  }

  trait Auth {

  }

}
