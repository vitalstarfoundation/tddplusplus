package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

object Browser {
  // https://github.com/scalaj/scalaj-http
  import scalaj.http._

  def get(url: String): String = {
    val response: HttpResponse[String] = Http(url).asString
    response.body
  }

  def post(url:String, param:Seq[(String, String)]):String = {
    val response: HttpResponse[String] = Http(url).postForm(param).asString
    response.body
  }
}

@RunWith(classOf[JUnitRunner])
class TestScalajHttp extends FunSuite with BeforeAndAfterAll {
  val BOARD_ID = "YHMv5s56"
  var appkey = ""
  var token = ""

  override def beforeAll() {
    import scala.io.Source
    val jsonStr =
          Source
            .fromFile("testdata/trello.json")
            .getLines
            .mkString

    val secret = Holder(jsonStr)
    appkey = secret.get("appkey").asString()
    token = secret.get("token").asString()
  }


  def getTrelloBoard(id: String): String = {
    val r = Browser.post("https://api.trello.com/1/boards/" + id,
      param = Seq("actions" -> "all",
        "boardStars"-> "none",
        "cards"-> "none",
        "card_pluginData" -> "false",
        "checklists" -> "none",
        "customFields" -> "false",
        "fields" -> "all",
        "lists" -> "open",
        "members" -> "none",
        "memberships" -> "none",
        "membersInvited" -> "none",
        "membersInvited_fields" -> "all",
        "pluginData" -> "false",
        "organization" -> "false",
        "organization_pluginData" -> "false",
        "myPrefs" -> "false",
        "tags" -> "false",
        "key" -> appkey,
        "token" -> token)
    )
    r
  }

  test("smoke test Scalaj") {
    val page = Browser.get("https://github.com/scalaj/scalaj-http")
    assertTrue(page.lines.length > 10)
  }

}
