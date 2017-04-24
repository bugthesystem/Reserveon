package utils

import java.sql.Timestamp

import models.{ Account, OAuthClient }
import org.joda.time.DateTime
import services.{ AccountsService, OAuthClientsService }

import scala.concurrent.ExecutionContext

sealed class SampleDataSeed(
    val accountsService: AccountsService,
    val oAuthClientsService: OAuthClientsService
)(implicit executionContext: ExecutionContext) {

  def run(): Unit = {
    for {
      _ <- accountsService.createAccount(
        Account(Some(0), "test@reserveon.com", "9ee3bc4800194e65a7951662a4b69bdb", new Timestamp(new DateTime().getMillis))
      )
      _ <- oAuthClientsService.create(
        OAuthClient(Some(0), 1, "client_credentials", "test_client_id", "test_client_secret",
          Some("redirectUrl"),
          new Timestamp(new DateTime().getMillis))
      )
    } yield {
      println(s"Database initialized with default values for `test`")
    }
  }
}
