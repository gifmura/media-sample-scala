package models

import play.api.libs.json.{Json, OFormat}

case class Image(
    id: Long,
    entryId: Long,
    url: String,
)

object Image {
  implicit val imageFormat
    : OFormat[Image] = Json.format[Image]
}
