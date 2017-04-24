package models.db

import java.sql.Timestamp

import models.OAuthAccessToken
import services.{ DatabaseService, DatabaseServiceImpl }

trait OauthAccessTokenTable {
  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class OauthAccessTokens(tag: Tag) extends Table[OAuthAccessToken](tag, "oauth_access_tokens") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def accountId = column[Long]("account_id")

    def oauthClientId = column[Long]("oauth_client_id")

    def accessToken = column[String]("access_token")

    def refreshToken = column[String]("refresh_token")

    def createdAt = column[Timestamp]("createdAt")

    def * = (id, accountId, oauthClientId, accessToken, refreshToken, createdAt) <> (OAuthAccessToken.tupled, OAuthAccessToken.unapply)

    //OauthAccessToken - Account     FK is defined in sql script and will be executed by FlyWay migration
    //OauthAccessToken - OAuthClient FK is defined in sql script and will be executed by FlyWay migration
  }

  protected val oauthAccessTokens = TableQuery[OauthAccessTokens]
}
