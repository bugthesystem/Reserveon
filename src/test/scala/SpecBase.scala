
import java.sql.Timestamp

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import handlers.OAuth2DataHandler
import http.HttpService
import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }
import org.scalatest.{ Matchers, WordSpec }
import org.specs2.mock.Mockito
import services._
import io.circe.generic.auto._
import io.circe.syntax._

//TODO: before after
trait SpecBase extends WordSpec with Matchers with ScalatestRouteTest with Mockito with FailFastCirceSupport {

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

  val databaseService: DatabaseService = mock[DatabaseService]
  val accountsService: AccountsService = mock[AccountsService]
  val oAuthClientsService: OAuthClientsService = mock[OAuthClientsService]
  val oAuthAccessTokensService: OAuthAccessTokensServiceImpl = mock[OAuthAccessTokensServiceImpl]
  val cacheService: CachingService = mock[CachingService]
  val moviesService: MoviesService = mock[MoviesService]
  val reservationService: ReservationService = mock[ReservationService]

  val oauth2DataHandler = new OAuth2DataHandler(
    oAuthClientsService,
    oAuthAccessTokensService, accountsService
  )

  val httpService = new HttpService(moviesService, oAuthClientsService,
    oAuthAccessTokensService, accountsService, cacheService, reservationService)
}