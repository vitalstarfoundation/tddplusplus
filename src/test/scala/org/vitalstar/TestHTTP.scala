package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Framing
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{ Failure, Success }
import scala.collection.mutable.StringBuilder

object Curl {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    def fetchRequest(request: HttpRequest) : String = {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      val result = Await.result(responseFuture, 5 second)
      val sb = StringBuilder.newBuilder
      result
        .entity
        .dataBytes
        .map(_.utf8String)
        .runForeach(data => sb.append(data))
      sb.toString()
    }
}

@RunWith(classOf[JUnitRunner])
class TestHTTP extends FunSuite with BeforeAndAfterAll {

  /*
  		https://doc.akka.io/docs/akka-http/current/client-side/request-level.html
  		https://stackoverflow.com/questions/32315789/akka-httpresponse-read-body-as-string-scala
   */
  ignore("Smoke Test") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://akka.io"))
    val result = Await.result(responseFuture, 5 second)
    val sb = StringBuilder.newBuilder
    result.entity.dataBytes.map(_.utf8String).runForeach(data => sb.append(data))

    assertTrue(sb.lines.length > 10)

//    val response = HttpResponse()
//    responseFuture
//      .onComplete {
//        case Success(response) => println(response)
//        case Failure(_)   => sys.error("something wrong")
//      }
  }

  test("test Curl") {
    val request = HttpRequest(uri = "https://akka.io")
    val s = Curl.fetchRequest(request)
    assertTrue(s.lines.length > 10)
    Thread.sleep(5000)
  }
}
