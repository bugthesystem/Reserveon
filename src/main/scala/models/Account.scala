package models

import java.sql.Timestamp

case class Account(id: Option[Long], email: String, password: String, createdAt: Timestamp)

