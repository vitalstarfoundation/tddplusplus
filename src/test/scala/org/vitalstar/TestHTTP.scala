package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import scala.concurrent.Future
import scala.util.{ Failure, Success }

@RunWith(classOf[JUnitRunner])
class TestHTTP extends FunSuite with BeforeAndAfterAll {

  // https://doc.akka.io/docs/akka-http/current/client-side/request-level.html
  test("Smoke Test") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    HttpRequest(uri = "https://akka.io")
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://akka.io"))

    val response = HttpResponse()
    responseFuture
      .onComplete {
        case Success(response) => println(response)
        case Failure(_)   => sys.error("something wrong")
      }
  }
}
