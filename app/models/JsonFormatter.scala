package models
import java.sql.Timestamp

import play.api.libs.json._

trait JsonFormatter {
  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

  implicit val formatTimestamp: Format[Timestamp] = new Format[Timestamp] {
    def writes(ts: Timestamp): JsValue = {
      JsString(dateFormat.format(ts))
    }
    def reads(ts: JsValue): JsResult[Timestamp] = {
      try {
        val date = dateFormat.parse(ts.as[String])
        JsSuccess(new Timestamp(date.getTime))
      } catch {
        case _: IllegalArgumentException =>
          JsError("Unable to parse timestamp")
      }
    }
  }
}
