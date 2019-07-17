package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{ Failure, Success }
import scala.collection.mutable.StringBuilder
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import org.apache.spark.sql.catalyst.expressions.AssertTrue

object Curl {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    def fetchRequest(request: HttpRequest) : String = {
      import scala.concurrent.duration.FiniteDuration
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://doc.akka.io/docs/akka-http/current/implications-of-streaming-http-entity.html"))
      val sb = StringBuilder.newBuilder

      responseFuture
        .onComplete {
          case Success(res) => {
            val f = res
              .entity
              .dataBytes
              .runFold(ByteString.empty)(_ ++ _)
              .foreach{body =>
                println(body.utf8String)
                sb.append(body.utf8String)
              }
          }
          case Failure(_) => {
            sys.error("something wrong")
            sb.append("something wrong")
          }
        }
      val result = Await.ready(responseFuture, Duration.Inf)
      sb.toString()

//      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
//      val result = Await.result(responseFuture, 5 second)
//      val sb = StringBuilder.newBuilder
//      result
//        .entity
//        .dataBytes
//        .map(_.utf8String)
//        .runForeach(data => sb.append(data))
//      sb.toString()
    }

    def shutdown() {
      Thread.sleep(1000L)
      Await.result(system.terminate(), Duration.Inf)
    }
}

@RunWith(classOf[JUnitRunner])
class TestHTTP extends FunSuite with BeforeAndAfterAll {

  ignore("Smoke Test2") {
    val request = HttpRequest(uri = "https://doc.akka.io/docs/akka-http/current/implications-of-streaming-http-entity.html")
    val s = Curl.fetchRequest(request)
    assertTrue(s.lines.length > 10)
    Curl.shutdown()
  }

  /*
  		https://doc.akka.io/docs/akka-http/current/client-side/request-level.html
  		https://stackoverflow.com/questions/32315789/akka-httpresponse-read-body-as-string-scala
  		https://stackoverflow.com/questions/49163000/dead-letters-using-akka-http-client-and-akka-streams
   */
  ignore("Smoke Test") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

/*
		import akka.http.scaladsl.unmarshalling.Unmarshal
		val responseAsString: Future[String] = Unmarshal(entity).to[String]

		val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://akka.io"))
    val result = Await.result(responseFuture, 5 second)

		val responseContent: Future[Option[String]] =
        response.entity.dataBytes.map(_.utf8String).runWith(Sink.lastOption)
      val content: Option[String] = Await.result(responseContent, 10.seconds)

        case Success(response) => {
          val x = response.entity.toStrict(3.second).flatMap { e =>
            e.dataBytes
            .runFold(ByteString.empty) { case (acc, b) => acc ++ b }
          }
          println(x)

        case Success(response) => {
          response.entity.dataBytes.runForeach{data =>
              println(data)
              sb.append(data)
            }
        }

  // https://github.com/akka/akka-http/issues/497
  val request = HttpRequest(uri = "/docs/akka-http/current/implications-of-streaming-http-entity.html")
  val sb = StringBuilder.newBuilder

  val flow = Http(system).outgoingConnectionHttps("doc.akka.io")

  val source = Source.single(request)
    .via(flow)
    .runWith(Sink.head)
//    .map{
//      _.entity.dataBytes
//      .runFold(ByteString.empty)(_ ++ _)
//      .foreach (body => sb.append(body.utf8String))
//    }

val strictEntity: Future[HttpEntity.Strict] = response.entity.toStrict(3.seconds)

// while API remains the same to consume dataBytes, now they're in memory already:
val transformedData: Future[ExamplePerson] =
  strictEntity flatMap { e =>
    e.dataBytes
      .runFold(ByteString.empty) { case (acc, b) => acc ++ b }
      .map(parse)
  }

requests hang if pool is shutdown before receiving response #1245
https://github.com/akka/akka-http/issues/1245

https://github.com/brharrington/akka-http-issue1245/blob/master/src/main/scala/example/Issue1245.scala
 */
    import scala.concurrent.duration.FiniteDuration
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://doc.akka.io/docs/akka-http/current/implications-of-streaming-http-entity.html"))
    val sb = StringBuilder.newBuilder

    var done = false
    var i = 0
    var s1 = responseFuture.isCompleted
    var s2 = responseFuture.isCompleted
    var s3 = responseFuture.isCompleted
    responseFuture
      .onComplete {
        case Success(res) => {
          val f = res
            .entity
            .dataBytes
            .runFold(ByteString.empty)(_ ++ _)
            .foreach{body =>
              println(body.utf8String)
              sb.append(body.utf8String)
              i += 1
            }
//          val strictEntity: Future[HttpEntity.Strict] = res.entity.toStrict(5 seconds)
//          val x = strictEntity.flatMap { e =>
//            e.dataBytes
//              .runFold(ByteString.empty)(_ ++ _)
//              .map(_.utf8String)
//          }
//          val html = Await.result(x,Duration.Inf)
//          println(html)
//          sb.append(html)

          done = true
          s2 = responseFuture.isCompleted
        }
        case Failure(_) => {
          sys.error("something wrong")
          sb.append("something wrong")
        }
      }
    s3 = responseFuture.isCompleted
    val result = Await.ready(responseFuture, Duration.Inf)

//    Http().shutdownAllConnectionPools().andThen {
//      case _ =>
//        materializer.shutdown()
//        system.terminate()
//    }
    import akka.actor.CoordinatedShutdown
//    CoordinatedShutdown(system).run()
    Thread.sleep(1000L)
    Await.result(system.terminate(), Duration.Inf)
//    Await.result(system.whenTerminated, Duration.Inf)
    if (!done) {
      println("not done")
    } else {
      println(sb.lines.length)
      println(i)
      println(done)
      println
      println(s1)
      println(s2)
      println(s3)
      println(responseFuture.isCompleted)
      assertTrue(sb.lines.length > 10)
    }
  }


//  test("test Curl") {
//    val request = HttpRequest(uri = "https://akka.io")
//    val s = Curl.fetchRequest(request)
//    assertTrue(s.lines.length > 10)
//    Thread.sleep(5000)
//  }

/*
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{ Failure, Success }
import scala.collection.mutable.StringBuilder
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Sink
import akka.util.ByteString

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()
implicit val executionContext = system.dispatcher



//https://developer.lightbend.com/guides/akka-http-quickstart-scala/

//https://doc.akka.io/docs/akka-http/current/client-side/connection-level.html
val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://akka.io"))


---------

val request = HttpRequest(uri = "/docs/akka-http/current/implications-of-streaming-http-entity.html")
val sb = StringBuilder.newBuilder

val flow = Http(system).outgoingConnectionHttps("doc.akka.io")

val source = Source.single(request)
  .via(flow)
  .runWith(Sink.head)


[ERROR] [07/14/2019 23:17:18.355] [default-akka.actor.default-dispatcher-2] [akka.dispatch.Dispatcher] Materializer shutdown while materializing stream
java.lang.IllegalStateException: Materializer shutdown while materializing stream
	at akka.stream.impl.PhasedFusingActorMaterializer.shutdownWhileMaterializingFailure$1(PhasedFusingActorMaterializer.scala:502)
	at akka.stream.impl.PhasedFusingActorMaterializer.materialize(PhasedFusingActorMaterializer.scala:511)
	at akka.stream.impl.PhasedFusingActorMaterializer.materialize(PhasedFusingActorMaterializer.scala:413)
	at akka.stream.impl.PhasedFusingActorMaterializer.materialize(PhasedFusingActorMaterializer.scala:408)
	at akka.stream.scaladsl.RunnableGraph.run(Flow.scala:556)
	at akka.stream.scaladsl.Source.runWith(Source.scala:103)
	at akka.stream.scaladsl.Source.runFold(Source.scala:113)
	at org.vitalstar.TestHTTP$$anonfun$1$$anonfun$apply$mcV$sp$1.apply(TestHTTP.scala:123)
	at org.vitalstar.TestHTTP$$anonfun$1$$anonfun$apply$mcV$sp$1.apply(TestHTTP.scala:118)
	at scala.concurrent.impl.CallbackRunnable.run(Promise.scala:32)
	at akka.dispatch.BatchingExecutor$AbstractBatch.processBatch(BatchingExecutor.scala:55)
	at akka.dispatch.BatchingExecutor$BlockableBatch$$anonfun$run$1.apply$mcV$sp(BatchingExecutor.scala:91)
	at akka.dispatch.BatchingExecutor$BlockableBatch$$anonfun$run$1.apply(BatchingExecutor.scala:91)
	at akka.dispatch.BatchingExecutor$BlockableBatch$$anonfun$run$1.apply(BatchingExecutor.scala:91)
	at scala.concurrent.BlockContext$.withBlockContext(BlockContext.scala:72)
	at akka.dispatch.BatchingExecutor$BlockableBatch.run(BatchingExecutor.scala:90)
	at akka.dispatch.TaskInvocation.run(AbstractDispatcher.scala:40)
	at akka.dispatch.ForkJoinExecutorConfigurator$AkkaForkJoinTask.exec(ForkJoinExecutorConfigurator.scala:43)
	at akka.dispatch.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
	at akka.dispatch.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
	at akka.dispatch.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
	at akka.dispatch.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)


*/
}
