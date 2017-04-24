package services

import models.{ Account, OAuthClient }
import models.db.OAuthClientsTable

import scala.concurrent.{ ExecutionContext, Future }

trait OAuthClientsService extends OAuthClientsTable {

  def create(client: OAuthClient): Future[OAuthClient]

  def validate(clientId: String, clientSecret: String, grantType: String): Future[Boolean]

  def findClientCredentials(clientId: String, clientSecret: String): Future[Option[Account]]

  def findClientByClientId(clientId: Long): Future[Option[OAuthClient]]

  def findByClientId(clientId: String): Future[Option[OAuthClient]]

  def tableQ() = oauthClients
}

//extends OAuthClientsTable
class OAuthClientsServiceImpl(
  val databaseService: DatabaseService,
  val accountsService: AccountsService
)(implicit executionContext: ExecutionContext)
    extends OAuthClientsService {

  import databaseService._
  import databaseService.driver.api._

  override def create(client: OAuthClient): Future[OAuthClient] = db.run(oauthClients returning oauthClients += client)

  override def validate(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = {
    db.run(oauthClients.filter(oauthClient => oauthClient.clientId === clientId && oauthClient.clientSecret === clientSecret).result)
      .map(_.headOption.exists(client => grantType == client.grantType || grantType == "refresh_token"))
  }

  override def findClientCredentials(clientId: String, clientSecret: String): Future[Option[Account]] = {
    for {
      accountId <- db.run(oauthClients.filter(oauthClient => oauthClient.clientId === clientId && oauthClient.clientSecret === clientSecret).result).map(_.headOption.map(_.ownerId))
      account <- accountsService.findAccountById(accountId.get)
    } yield account
  }

  override def findClientByClientId(clientId: Long): Future[Option[OAuthClient]] = db.run(oauthClients.filter(_.id === clientId).result.headOption)

  override def findByClientId(clientId: String): Future[Option[OAuthClient]] = db.run(oauthClients.filter(_.clientId === clientId).result.headOption)
}
