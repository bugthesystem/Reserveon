package models.db

import java.sql.Timestamp

import models.OAuthClient
import services.DatabaseService

trait OAuthClientsTable extends AccountsTable {
  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class OauthClients(tag: Tag) extends Table[OAuthClient](tag, "oauth_clients") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def ownerId = column[Long]("owner_id")

    def grantType = column[String]("grant_type")

    def clientId = column[String]("client_id")

    def clientSecret = column[String]("client_secret")

    def redirectUri = column[Option[String]]("redirect_uri")

    def createdAt = column[Timestamp]("createdAt")

    def * = (id, ownerId, grantType, clientId, clientSecret, redirectUri, createdAt) <> (OAuthClient.tupled, OAuthClient.unapply)

    //OauthClient - Account FK is defined in sql script and will be executed by FlyWay migration
  }

  protected val oauthClients = TableQuery[OauthClients]
}
