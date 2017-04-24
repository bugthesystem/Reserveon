package models.db

import java.sql.Timestamp

import models.Movie
import services.DatabaseService

trait MovieEntityTable {

  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class Movies(tag: Tag) extends Table[Movie](tag, "movies") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def imdbId = column[String]("imdbId")

    def movieTitle = column[String]("movieTitle")

    def availableSeats = column[Int]("availableSeats")

    def screenId = column[String]("screenId")

    def createdAt = column[Timestamp]("createdAt")

    def * = (id, imdbId, movieTitle, availableSeats, screenId, createdAt) <> (Movie.tupled, Movie.unapply)
  }

  protected val movies = TableQuery[Movies]

}

