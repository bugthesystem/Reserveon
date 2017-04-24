package utils

import java.sql.Timestamp

import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }

trait CirceCommonCodecs {

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }
}
