package models

import java.sql.Timestamp
import java.util.Date

import jp.t2v.lab.play2.pager.{OrderType, Sortable}
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

  val SORT_KEY_ID = "id"
  val SORT_KEY_TITLE = "title"
  val SORT_KEY_CREATE = "create_time"
  val SORT_KEY_UPDATE = "update_time"

  implicit object sortable extends Sortable[Entry] {
    def default: (String, OrderType) = (SORT_KEY_ID, OrderType.Descending)
    def acceptableKeys: Set[String] =
      Set(SORT_KEY_ID, SORT_KEY_TITLE, SORT_KEY_CREATE, SORT_KEY_UPDATE)
  }
}
