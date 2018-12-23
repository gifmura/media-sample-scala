package models

import play.api.libs.json.{Json, OFormat}

case class Image(
    id: Long,
    entry_id: Long,
    url: String,
    size: Long
)

object Image {
  implicit val imageFormat: OFormat[Image] = Json.format[Image]
}
