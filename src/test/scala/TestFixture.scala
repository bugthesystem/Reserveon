import java.sql.Timestamp

import models.{ Account, OAuthAccessToken, OAuthClient }
import org.joda.time.DateTime

trait TestFixture {
  val createdAt = new Timestamp(DateTime.now().getMillis)
  val testClient = OAuthClient(Option(1), 1, "authorization_code", "test_client_id", "test_client_secret",
    Some("http://localhost:3000/callback"), createdAt)
  val testUser = Account(Option(1), "testmail@gmail.com", "pass", createdAt)
  val testToken = OAuthAccessToken(Option(1), 1, 1, "valid token", "refresh token", createdAt)
}
