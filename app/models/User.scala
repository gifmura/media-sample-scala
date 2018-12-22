package models

import java.sql.Timestamp
import java.util.Date

import play.api.libs.json.{Json, OFormat}

case class User(id: Long,
                email: String,
                password: String,
                name: String,
                user_type: String = User.TYPE_NORMAL,
                registration_time: Timestamp = new Timestamp(
                  new Date().getTime),
                status: String = User.STATUS_ACTIVE
               )

object User extends JsonFormatter {
  implicit val userFormat: OFormat[User] = Json.format[User]
  val TYPE_NORMAL = "NORMAL"
  val TYPE_ADMIN = "ADMIN"
  val STATUS_ACTIVE = "ACTIVE"
  val STATUS_DELETED = "DELETED"
}
