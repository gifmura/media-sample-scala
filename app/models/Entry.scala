package models

import java.sql.Timestamp
import java.util.Date

import play.api.libs.json.{Json, OFormat}

case class Entry(
    id: Long,
    user_id: Long,
    title: String,
    content: String,
    create_time: Timestamp = new Timestamp(new Date().getTime),
    update_time: Timestamp = new Timestamp(new Date().getTime),
    status: String = Entry.STATUS_ACTIVE
)

object Entry extends JsonFormatter {
  implicit val entryFormat: OFormat[Entry] = Json.format[Entry]
  val STATUS_ACTIVE = "ACTIVE"
  val STATUS_DELETED = "DELETED"
  val STATUS_DRAFT = "DRAFT"
}
