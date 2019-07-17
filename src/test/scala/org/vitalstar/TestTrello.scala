package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

import requests.Response
import play.api.libs.json._
import play.api.libs.json.JsLookupResult.jsLookupResultToJsLookup
import play.api.libs.json.JsValue.jsValueToJsLookup

@RunWith(classOf[JUnitRunner])
class TestTrello extends FunSuite with BeforeAndAfterAll {
  val BOARD_ID = "YHMv5s56"
  val keyValue = "4ac9b4b9148d6219878a27ee0dbe1453"
  val tokenValue = "fcb485ddab5de34e7de2b19f4e22f2cd8904fbacddfaf6450303d53a7bf1bd43"

  // Perform GET request to retrieve Trello board name
  def getTrelloBoard(id: String): Response = {
    val r = requests.get("https://api.trello.com/1/boards/" + id,
      params = Map("actions" -> "all",
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
        "key" -> keyValue,
        "token" -> tokenValue)
    )
    r
  }

  def getCards(id : String): Response = {
    val r = requests.get("https://api.trello.com/1/boards/" + id + "/cards",
      params = Map(
        "key" -> keyValue,
        "token" -> tokenValue)
    )
    r
  }

  def getList(id : String): Response = {
    val r = requests.get("https://api.trello.com/1/lists/" + id + "/cards",
      params = Map(
        "key" -> keyValue,
        "token" -> tokenValue
      ))
    r
  }

  def getListID(name : String): String = {
    var board = Json.parse(getTrelloBoard(BOARD_ID).text)
    var iter = 0
    var listIndex = -1
    // iterate through to get specific list
    (board \"lists" \\ "name").foreach(x =>
    {
      if(x.as[String] == name){
        listIndex = iter
      }
      iter += 1
    }
    )
    assert(listIndex != -1, "List couldn't be found")

    val listID = (board \ "lists" \ listIndex \"id").as[String]
    listID
  }

  def getCardID(name:String): String = {
    val r = getCards(BOARD_ID)
    val cards = Json.parse(r.text)

    var iter = 0
    var cardIndex = -1
    // iterate through and get all the names to check for specific one
    (cards \\ "name").foreach(x => {
      if(x.as[String].equals(name)){
        cardIndex = iter
      }
      iter += 1
    })
    assert(cardIndex != -1, "Card couldn't be found")

    iter = 0
    var savedID = ""
    // iterate through and get the ID for specific card
    (cards \\ "id").foreach(x =>{
      if(iter == cardIndex){
        savedID = x.as[String]
      }
      iter += 1
    })
    savedID
  }

  test("[Requests] Get Trello Board"){
    val r = getTrelloBoard(BOARD_ID)
    val board = Json.parse(r.text)
    val boardName = (board \ "name").as[String]
    assert(boardName == "JEN Board")
  }

  test("[Requests] Get Cards"){
    val r = getCards(BOARD_ID)
    val cards = Json.parse(r.text)
    assert(r.statusCode == 200)
  }

  test("[Requests] Create New Card"){
    val board = Json.parse(getTrelloBoard(BOARD_ID).text)
    val listID = (board \ "lists" \ 0 \"id").as[String]
    val r = requests.post("https://api.trello.com/1/cards",
      params = Map(
        "name" -> "Test Create Card",
        "desc" -> "A card created by TDD",
        "idList" -> listID,
        "key" -> keyValue,
        "token" -> tokenValue
      )
    )
    assert(r.statusCode == 200)
  }


  test("[Requests] Get Specific Card"){

    val cardID = getCardID("Test Create Card")

    val r = requests.get("https://api.trello.com/1/cards/" + cardID,
      params = Map(
        "key" -> keyValue,
        "token" -> tokenValue
      ))
    val cardJson = Json.parse(r.text)
    assert( (cardJson \ "name").as[String] == "Test Create Card" )
  }



  test("[Requests] Update Specific Card"){
    val cardID = getCardID("Test Create Card")
    val r = requests.put("https://api.trello.com/1/cards/" + cardID,
      params = Map(
        "name" -> "Test Updated Name2",
        "key" -> keyValue,
        "token" -> tokenValue
      ))
    assert(r.statusCode == 200)
  }



  test("[Requests] Delete Specific Card"){
    val cardID = getCardID("Test Updated Name2")

    val r_d = requests.delete("https://api.trello.com/1/cards/" + cardID,
      params= Map(
        "key" -> keyValue,
        "token" -> tokenValue
      ))

    assert(r_d.statusCode == 200)
  }
}
