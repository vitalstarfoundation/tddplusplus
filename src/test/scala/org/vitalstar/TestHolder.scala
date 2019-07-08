package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

case class Winner(val winnerId:Int, val numbers:List[Int])
  // cannot be nested!!!

@RunWith(classOf[JUnitRunner])
class TestHolder extends FunSuite with BeforeAndAfterAll {
  var lottoHolder: Holder = _
  var winners: Holder = _

  override def beforeAll() {
//    super.beforeAll()
    cleanUp()  // in case something left over from last test
    lottoHolder = Holder(lotto)
  }

  override def afterAll() {
    try {
      cleanUp()
    } finally {
//      super.afterAll()
    }
  }

  def cleanUp() {}

  test("smoke test") {


  }

  test("readJson") {
    import scala.util.parsing.json._
    import scala.io.Source
    val configString = Source.fromFile("testdata/aws.json").getLines.mkString
    val result = JSON.parseFull(configString)
    val keyfile = result.get.asInstanceOf[Map[String, Any]]
    assertEquals("--accesskey--", keyfile("AWSAccessKey"))
    assertEquals("--accesskeyid--", keyfile("AWSAccessKeyId"))
  }

  val lotto = """
    {
      "id": "1234",
      "lotto":{
        "lotto-id":5,
        "winning-numbers":[2,45,34,23,7,5,3],
        "lucky-number":[7],
        "winners":[ {
          "winnerId":23,
          "numbers":[2,45,34,23,3, 5]
        },{
          "winnerId" : 54 ,
          "numbers":[ 52,3, 12,11,18,22 ]
        }, "ABC"],
        "wins":[ {
          "winnerId":23,
          "numbers":[2,45,34,23,3, 5]
        },{
          "winnerId" : 54 ,
          "numbers":[ 52,3, 12,11,18,22 ]
        }],
        "emptyArray": []
      }
    }
    """

  // https://github.com/json4s/json4s/blob/master/tests/src/test/scala/org/json4s/Examples.scala
  test("demo json4s") {
//    import org.json4s._
//    import org.json4s.JsonDSL._
//    import org.json4s.jackson.JsonMethods._
//    implicit val formats = DefaultFormats

    import org.json4s._
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._
    import org.json4s.{DefaultFormats, Formats}
    implicit val jsonFormats: Formats = DefaultFormats

    val x = parse(lotto)

    assertEquals(List(2,45,34,23,7,5,3), x \ "lotto" \ "winning-numbers" \ classOf[JInt])
    assertEquals(List(2,45,34,23,7,5,3), x \\ "winning-numbers" \ classOf[JInt])
    assertEquals(List(2,45,34,23,7,5,3), (x \\ "winning-numbers").extract[List[Int]])
    assertEquals(List("2","45","34","23","7","5","3"), (x \\ "winning-numbers").extract[List[String]])
    assertEquals(3, (x \\ "winners").extract[List[Any]].size)
    val winners =  (x \\ "wins").extract[List[Winner]]
    assertEquals(2, winners.size)
  }

  test("2nd demo json4s") {
    import org.json4s._
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._
    import org.json4s.{DefaultFormats, Formats}
    implicit val jsonFormats: Formats = DefaultFormats

    val x = parse(lotto)

    // due to additional "ABC" in the winners array
    // So using json4s directly will be in the mercy of well formed json
    // That is the reason we use Holder which is much more forgiving
    intercept[Exception] {
      val winners2 =  (x \\ "winners").extract[List[Winner]]
      assertEquals(2, winners2.size)
    }
  }

  test("HolderBasic") {
    assertEquals("1234", lottoHolder.get("id").asString("xyz"))
    assertEquals("xyz", lottoHolder.get("blah").asString("xyz"))
    assertTrue(lottoHolder.get("unknown").isEmpty)
    assertEquals("", lottoHolder.get("unknown").asString())
    assertEquals(2, lottoHolder.size)

    // (x\"lotto").extract[Map[String, JValue]]
    //val o2 = o1.getAsMapHolder("lotto")

    assertEquals("1234", lottoHolder.get(0).asString())
    assertEquals("", lottoHolder.get(1).asString())

    // TODO: string to int or int to string
    assertEquals(6, lottoHolder.get("lotto").size)
    assertEquals("5", lottoHolder.get("lotto").get("lotto-id").asString())
      // o1("lotto")("lotto-id")
    assertEquals(5, lottoHolder.get("lotto").get("lotto-id").asInt())
    assertEquals(5, lottoHolder.get("lotto").get(0).asInt())

    // Testing the boundaries
    assertEquals(2, lottoHolder.get("lotto").get("winning-numbers").get(0).asInt())
    assertEquals(3, lottoHolder.get("lotto").get("winning-numbers").get(6).asInt())

  }

  test("ListHolder") {
    assertEquals(3, lottoHolder.get("lotto").get("winners").size)
    winners = lottoHolder.get("lotto").get("winners").asListHolder
    assertEquals(3, winners.size)
    assertEquals("ABC", winners.get(2).asString())
    assertEquals("", winners.get(0).asString())  // winners(0) is not a String
    assertEquals(2, winners.get(1).size)  // winners(1) is an object with 2 elements
  }

  // https://github.com/json4s/json4s/issues/562
  test("EmptyList") {
    val emptyArrayValue = lottoHolder.get("lotto").get("emptyArray")
    val emptyArrayHolder = emptyArrayValue.asListHolder
    assertTrue(emptyArrayValue.isEmpty)
      // empty List itself is an object, but in order to make it consistent with
      // 3.5.2, we now consider it as empty, so we can't use empty array in JSON
    assertEquals(0, emptyArrayValue.size)
    assertTrue(emptyArrayHolder.isEmpty) // But asking a holder, holder consider it is empty
    assertEquals(0, emptyArrayHolder.size)

    // Testing empty holder
    assertEquals(Holder.EmptyHolder,emptyArrayHolder.get(0))
    assertEquals(Holder.EmptyHolder,emptyArrayHolder.get(10))
    assertEquals(Holder.EmptyHolder,emptyArrayHolder(0))
    assertEquals(Holder.EmptyHolder,emptyArrayHolder(10))
    assertEquals(Holder.EmptyHolder,emptyArrayHolder("haha"))
  }

  test("As[T]") {
    assertEquals("1234", lottoHolder.get(0).as[String](""))
    assertEquals("", lottoHolder.get(1).as[String](""))

    // Use as[T] type casting
    val winnersList: List[Any] = lottoHolder.get("lotto").get("winners").as[List[Any]](List())
    assertEquals(3, winnersList.size)
    val w0: Map[String,Any] = winnersList(0).asInstanceOf[Map[String,Any]]
    val w2: String = winnersList(2).asInstanceOf[String]
    assertEquals(2, w0.size)
    assertEquals("ABC", w2)
  }

  test("Find") {
    winners = lottoHolder.get("lotto").get("winners").asListHolder

    // Test find()
    assertEquals("5", lottoHolder.find("lotto-id").asString())
    assertEquals(5, lottoHolder.find("lotto-id").asInt())
    assertEquals("ABC", winners.get("2").asString())
      // automatically to try (Int)2 also to faciliate path()
    assertEquals(0, winners.find("2").asListHolder.size)
      // Find will try try (Int)2
    assertEquals(3, lottoHolder.find("winners").size)
  }

  test("Path") {
    // Test path()
    assertEquals(5, lottoHolder.path("lotto.lotto-id").asInt())
    assertEquals(5, lottoHolder.path("lotto.0").asInt())
    assertEquals(2, lottoHolder.path("lotto.winning-numbers.0").asInt())
    assertEquals(3, lottoHolder.path("lotto.winning-numbers.6").asInt())
    assertEquals(3, lottoHolder.path("lotto=winning-numbers=6", "=").asInt())
  }

  test("HolderCaseClass") {
    val wl = lottoHolder.get("lotto").get("wins")
    val winnersList2: List[Winner] = wl.as[List[Winner]](List())
    assertEquals(23, winnersList2(0).winnerId)
    assertEquals(45, winnersList2(0).numbers(1))
      // "winnerId":23,
      // "numbers":[2,45,34,23,3, 5]
    val winnersList3 = lottoHolder.find("wins").as[List[Winner]](List())
    assertEquals(45, winnersList3(0).numbers(1))

    val winnersList4: List[Winner] = lottoHolder.get("lotto").get("winners").as[List[Winner]](List[Winner]())
    assertEquals(0, winnersList4.size)
  }

  test("HolderCaseClass1") {
    val winnersList4: List[Winner] = lottoHolder.get("lotto").get("winners").as[List[Winner]](List[Winner]())
    assertEquals(0, winnersList4.size)
  }
}
