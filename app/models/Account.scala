package models

import play.api.libs.json.Json

case class  Account(id:Long, name:String)

object Account{
  implicit val accountFormat = Json.format[Account]
}
