package models

import java.sql.Timestamp

case class OAuthClient(
  id: Option[Long],
  ownerId: Long,
  grantType: String,
  clientId: String,
  clientSecret: String,
  redirectUri: Option[String],
  createdAt: Timestamp
)
