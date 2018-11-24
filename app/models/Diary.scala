package models

import play.api.libs.json.Json

case class Diary(
                id:Long,
                accountId:Long,
                imageId:Option[Long],
                title:String,
                body:String
                )

object Diary{
  implicit val diaryFormat = Json.format[Diary]
}