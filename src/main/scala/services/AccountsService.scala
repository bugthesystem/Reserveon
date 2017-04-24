package services

import java.security.MessageDigest

import models.Account
import models.db.AccountsTable

import scala.concurrent.{ ExecutionContext, Future }

trait AccountsService {

  def createAccount(account: Account): Future[Account]

  def authenticate(email: String, password: String): Future[Option[Account]]

  def findAccountById(id: Long): Future[Option[Account]]
}

class AccountsServiceImpl(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext)
    extends AccountsTable with AccountsService {

  import databaseService._
  import databaseService.driver.api._

  private def digestString(s: String): String = {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(s.getBytes)
    md.digest.foldLeft("") { (s, b) =>
      s + "%02x".format(if (b < 0) b + 256 else b)
    }
  }

  override def createAccount(account: Account): Future[Account] = db.run(accounts returning accounts += account)

  override def authenticate(email: String, password: String): Future[Option[Account]] = {
    val hashedPassword = digestString(password)

    db.run(accounts.filter(acc => acc.password === hashedPassword && acc.email === email).result.headOption)
  }

  override def findAccountById(id: Long): Future[Option[Account]] = db.run(accounts.filter(_.id === id).result.headOption)
}

