package models

import play.api.libs.json.Json

case class Image(
    id: Long,
    entryId: Long,
    url: String,
)

object Image {
  implicit val imageFormat = Json.format[Image]
}
