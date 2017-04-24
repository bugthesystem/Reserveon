package models

import java.sql.Timestamp

case class OAuthAccessToken(
  id: Option[Long],
  accountId: Long,
  oauthClientId: Long,
  accessToken: String,
  refreshToken: String,
  createdAt: Timestamp
)
