package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

@RunWith(classOf[JUnitRunner])
class TestAkka extends FunSuite with BeforeAndAfterAll {
  override def beforeAll() {
//  super.beforeAll()
    cleanUp()  // in case something left over from last test
  }

  override def afterAll() {
    try {
      cleanUp()
    } finally {
//    super.afterAll()
    }
  }

  def cleanUp() {}

  // http://localhost:8080/hello
  test("smoke test") {
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}
implicit val jsonFormats: Formats = DefaultFormats

implicit val system = ActorSystem("my-system")
implicit val materializer = ActorMaterializer()
// needed for the future flatMap/onComplete in the end
implicit val executionContext = system.dispatcher
val route=
  path("asHTML"){
//    parameters('c, 'b) {
//        (c, b) =>complete(s"The color is '$c' and the background is '$b'")
//    }
    parameters('id, 'text) {
      (id, text) => {
        val obj = TestClass1.findObject(id)
        if (TestClass1.emptyobj == obj) {
          complete(
               HttpEntity(
                   ContentTypes.`text/html(UTF-8)`
                   ,s"<p>object $id not found</p>"))
        } else {
          obj.setText(text)
          complete(
               HttpEntity(
                   ContentTypes.`text/html(UTF-8)`
                   ,obj.asHTML))
        }
      }
    } ~
    parameters('id) {
      (id) => {
        val obj = TestClass1.findObject(id)
        if (TestClass1.emptyobj == obj) {
          complete(
               HttpEntity(
                   ContentTypes.`text/html(UTF-8)`
                   ,s"<p>object $id not found</p>"))
        } else {
          complete(
               HttpEntity(
                   ContentTypes.`text/html(UTF-8)`
                   ,obj.asHTML))
        }
      }
    }

  } ~
  path("asJSON"){
    parameters('id) {
      (id) => complete(
             HttpEntity(
                 ContentTypes.`application/json`
                 ,compact(render(TestClass1.findObject(id).asJSON))))
    }
  } ~
  path("list"){
     get {
         complete(
             HttpEntity(
                 ContentTypes.`application/json`
                 ,compact(render(TestClass1.list))))
     }
}

val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

//println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
//StdIn.readLine() // let it run until user presses return
bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

  }


}

