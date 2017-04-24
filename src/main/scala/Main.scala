
import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import http.HttpService
import services._
import utils.{ Config, SampleDataSeed }

import scala.concurrent.ExecutionContext

object Main extends App with Config {

  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val flywayService = new FlywayServiceImpl(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabaseSchema()

  val databaseService = new DatabaseServiceImpl(jdbcUrl, dbUser, dbPassword)
  val accountsService = new AccountsServiceImpl(databaseService)
  val oAuthClientsService = new OAuthClientsServiceImpl(databaseService, accountsService)
  val oAuthAccessTokensService = new OAuthAccessTokensServiceImpl(databaseService, oAuthClientsService)
  val cacheService = new CachingServiceImpl(redisHost, redisPort)
  val moviesService = new MoviesServiceImpl(databaseService, cacheService)
  val reservationService = new ReservationServiceImpl(moviesService, cacheService)

  val httpService = new HttpService(moviesService, oAuthClientsService,
    oAuthAccessTokensService, accountsService, cacheService, reservationService)

  if (dbCreateSampleData) new SampleDataSeed(accountsService, oAuthClientsService)
    .run()

  Http().bindAndHandle(httpService.routes, httpHost, httpPort)
}
