package services

import java.sql.Timestamp

import redis.RedisClient
import akka.actor.ActorSystem
import io.circe.Decoder.Result
import models.{ Movie, ReservationCounter }
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }

trait CachingService {

  def addToCache(key: String, obj: Movie): Future[Boolean]

  def getFromCache(key: String): Future[Movie]

  //TODO:
  def addReservationToCache(key: String, obj: ReservationCounter): Future[Boolean]

  def getReservationStateFromCache(key: String): Future[Option[ReservationCounter]]

  def deleteFromCache(key: String): Future[Long]

  def existsInCache(key: String): Future[Boolean]
}

class CachingServiceImpl(host: String = "127.0.0.1", port: Int = 6379)(implicit
  executionContext: ExecutionContext,
  implicit val actorSystem: ActorSystem)
    extends CachingService {

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

  val redis = RedisClient(host = host, port = port)

  override def addToCache(key: String, obj: Movie): Future[Boolean] = redis.set(key, obj.asJson.noSpaces)

  override def getFromCache(key: String): Future[Movie] = {
    redis.get(key).map(maybeString => maybeString.get.utf8String).map(rawJson => {

      val decoded: Either[Error, Movie] = decode[Movie](rawJson)
      decoded match {
        case Right(m) => m
        case Left(_) => null
      }
    })
  }

  //TODO: Remove or make them generic
  override def addReservationToCache(key: String, obj: ReservationCounter): Future[Boolean] = redis.set(key, obj.asJson.noSpaces)

  override def getReservationStateFromCache(key: String): Future[Option[ReservationCounter]] = {
    redis.get(key).map(maybeString => maybeString.get.utf8String).map(rawJson => {

      val decoded: Either[Error, ReservationCounter] = decode[ReservationCounter](rawJson)
      decoded match {
        case Right(m) => Option(m)
        case Left(_) => None
      }
    })
  }

  override def deleteFromCache(key: String): Future[Long] = redis.del(key)

  override def existsInCache(key: String): Future[Boolean] = redis.exists(key)
}