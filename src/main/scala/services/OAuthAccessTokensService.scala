package services

import java.security.SecureRandom
import java.sql.Timestamp

import models.db.OauthAccessTokenTable
import models.{ Account, OAuthAccessToken, OAuthClient }
import org.joda.time.DateTime

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

trait OAuthAccessTokensService {

  def create(account: Account, client: OAuthClient): Future[OAuthAccessToken]

  def delete(account: Account, client: OAuthClient): Future[Int]

  def refresh(account: Account, client: OAuthClient): Future[OAuthAccessToken]

  def findByAuthorized(account: Account, clientId: String): Future[Option[OAuthAccessToken]]

  def findByAccessToken(accessToken: String): Future[Option[OAuthAccessToken]]

  def findByRefreshToken(refreshToken: String): Future[Option[OAuthAccessToken]]
}

class OAuthAccessTokensServiceImpl(val databaseService: DatabaseService, val oAuthClientsService: OAuthClientsService)(implicit executionContext: ExecutionContext)
    extends OauthAccessTokenTable with OAuthAccessTokensService {

  import databaseService._
  import databaseService.driver.api._

  override def create(account: Account, client: OAuthClient): Future[OAuthAccessToken] = {
    def randomString(length: Int) = new Random(new SecureRandom()).alphanumeric.take(length).mkString

    val accessToken = randomString(40)
    val refreshToken = randomString(40)
    val createdAt = new Timestamp(new DateTime().getMillis)
    val oauthAccessToken = new OAuthAccessToken(
      id = Option(0),
      accountId = account.id.get,
      oauthClientId = client.id.get,
      accessToken = accessToken,
      refreshToken = refreshToken,
      createdAt = createdAt
    )

    //.map(id => oauthAccessToken.copy(id = id))
    db.run(oauthAccessTokens returning oauthAccessTokens += oauthAccessToken)
  }

  override def delete(account: Account, client: OAuthClient): Future[Int] = {
    db.run(oauthAccessTokens.filter(oauthToken => oauthToken.accountId === account.id && oauthToken.oauthClientId === client.id).delete)
  }

  override def refresh(account: Account, client: OAuthClient): Future[OAuthAccessToken] = {
    delete(account, client)
    create(account, client)
  }

  override def findByAuthorized(account: Account, clientId: String): Future[Option[OAuthAccessToken]] = {
    val query = for {
      oauthClient <- oAuthClientsService.tableQ()
      token <- oauthAccessTokens if oauthClient.id === token.oauthClientId && oauthClient.clientId === clientId && token.accountId === account.id
    } yield token
    db.run(query.result).map(_.headOption)
  }

  override def findByAccessToken(accessToken: String): Future[Option[OAuthAccessToken]] = {
    db.run(oauthAccessTokens.filter(_.accessToken === accessToken).result).map(_.headOption)
  }

  override def findByRefreshToken(refreshToken: String): Future[Option[OAuthAccessToken]] = {
    val expireAt = new Timestamp(new DateTime().minusMonths(1).getMillis)
    db.run(oauthAccessTokens.filter(token => token.refreshToken === refreshToken && token.createdAt > expireAt).result).map(_.headOption)

  }
}
