package models

import play.api.libs.json.Json

case class Entry(
    id: Long,
    accountId: Long,
    title: String,
    body: String
)

object Entry {
  implicit val entryFormat = Json.format[Entry]
}
