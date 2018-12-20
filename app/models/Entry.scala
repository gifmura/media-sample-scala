package models

import play.api.libs.json.{Json, OFormat}

case class Entry(
    id: Long,
    accountId: Long,
    title: String,
    body: String
)

object Entry {
  implicit val entryFormat: OFormat[Entry] = Json.format[Entry]
}
