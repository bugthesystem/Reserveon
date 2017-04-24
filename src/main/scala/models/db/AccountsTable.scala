package models.db

import java.sql.Timestamp

import models.Account
import services.{ DatabaseService, DatabaseServiceImpl }

trait AccountsTable {
  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def password = column[String]("password")
    def createdAt = column[Timestamp]("createdAt")

    def * = (id, email, password, createdAt) <> (Account.tupled, Account.unapply)
  }

  protected val accounts = TableQuery[Accounts]
}

