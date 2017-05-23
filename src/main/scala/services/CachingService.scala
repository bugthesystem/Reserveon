package services

import akka.actor.ActorSystem
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import redis.RedisClient
import utils.CirceCommonCodecs

import scala.concurrent.{ ExecutionContext, Future }

trait CachingService {

  def addToCache[T](key: String, obj: T)(implicit encoder: Encoder[T]): Future[Boolean]

  def getFromCache[T](key: String)(implicit decoder: Decoder[T]): Future[Option[T]]

  def deleteFromCache(key: String): Future[Long]

  def existsInCache(key: String): Future[Boolean]
}

class CachingServiceImpl(host: String = "127.0.0.1", port: Int = 6379)(implicit
  executionContext: ExecutionContext,
  implicit val actorSystem: ActorSystem)
    extends CachingService with CirceCommonCodecs {

  val redis = RedisClient(host = host, port = port)

  override def addToCache[T](key: String, obj: T)(implicit encoder: Encoder[T]): Future[Boolean] = redis.set(key, obj.asJson.noSpaces)

  override def getFromCache[T](key: String)(implicit decoder: Decoder[T]): Future[Option[T]] = {
    redis.get(key).map(maybeString => maybeString.get.utf8String).map(rawJson => {

      val decoded: Either[Error, T] = decode[T](rawJson)
      decoded match {
        case Right(m) => Some(m)
        case Left(_) => None
      }
    })
  }

  override def deleteFromCache(key: String): Future[Long] = redis.del(key)

  override def existsInCache(key: String): Future[Boolean] = redis.exists(key)
}