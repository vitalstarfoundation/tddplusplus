package org.vitalstar

/*
Here is a programming problem to solve that shouldn't use up much time and I can speak to references for further qualification.

===
Provide some statistics based on log file format below.

Output should look similar to this.

-- Result Codes (most frequent to least) --
200 (Success) - 256 occurrences
403 (Unauthorized) - 10 occurrences
205 (Reset content) - 8 occurrences

-- Sources (most frequent to least) --
64.242.88.10 - 100 occurrences
lj1036.inktomisearch.com - 20 occurrences

-- Bytes Distribution (percentiles) --
25th Percentile: 124
50th Percentile:  3518
75th Percentile: 93593


Python is preferred but it's up to you.

Evaluation

● Correctness: solution has to work and gracefully handle errors
● Readability and maintainability: code readable and extensible
● Discussion after solution is submitted on trade-offs or related issues

You can use Internet and do like you would if given as real problem.
 */

import org.scalatest.junit.JUnitSuite
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.junit.AssertionsForJUnit

import scala.io.Source
import java.io.FileNotFoundException
import java.util.Random

class UtilsTest
        extends JUnitSuite
        with org.scalatest.Matchers
        with AssertionsForJUnit {
  /*
   * helper class to simplify content access
   */
  case class LogLine(src:String, x1:String, x2:String, ts:String, action:String,
                code:String, byte:String)

  /*
   * Helper class for reservior sampling
   * ref: http://en.wikipedia.org/wiki/Reservoir_sampling
   */
  class Reservior(val capacity: Int, val seed: Int = 1234) {
    var array = new Array[String](capacity)
    var size = 0
    val rand = new Random(seed) // fix randomness for easy testing

    def sample(element: Int): Reservior = sample(element.toString)
    def sample(element: String): Reservior = {
      if (size < capacity) {
        array(size) = element
      } else {
        val randomPos = (rand.nextDouble()*size).toInt
        if (randomPos < capacity) {
          array(randomPos) = element
        }
      }
      size += 1
      this
    }

    // return a sorted sample array of String
    def sorted: Array[String] = {
      val sorted = if (size < capacity) {
        array.slice(0,size)
      } else {
        array
      }
      sorted.sortWith((a,b) => a < b)
    }

    // convert String to Int with default in case it is NAN
    def asInt(value: String, default: Int = 0): Int = {
      try {
        value.toInt
      } catch {
        case _: Throwable => default
      }
    }

    // return a sorted sample array of Int
    def sortedAsInt: Array[String] = {
      val sorted = if (size < capacity) {
        array.slice(0,size)
      } else {
        array
      }
      sorted.sortWith{ (a,b) => asInt(a) < asInt(b) }
    }

  }

  /*
   * Helper method to create an iterator of LogLines from the web log
   *
   * Invalid log line will be eliminated by flatMap, and such log line will be
   * print in stderr for checking.
   * In case of non-existing file, it will return an empty iterator.
   */
  def getLogLines(filename: String) = {
    val pattern = """^(\S+) (\S) (\S) \[(.*)\] "(.*)" (\S+) (\S+)""".r

    // Reading log file
    val logs = try {
      Source.fromFile(filename)
    } catch {
      case e: FileNotFoundException => null
      case _: Throwable => null
    }
    if (logs == null) {
      Iterator.empty
    } else {
      val lines = logs.getLines
      val logLines = lines.flatMap{line:String =>
          line match {
            case pattern(src, x1, x2, ts, action, code, byte) => Some(new LogLine(src, x1, x2, ts, action, code, byte))
            case pattern(_*) => {
              System.err.println(s"$line\n not parsable")
              None
            }
            case _ => {
              System.err.println(s"$line\n not parsable")
              None
            }
          }
        }
      logLines
    }
  }

  /*
   * Smoke test some basic mechanics
   */
  @Test def smokeTest {
    // FileNotFound Exception for non-existent file
    val error = try {
      val source1 = Source.fromFile("nofile")
      source1.getLines.length.toString
    } catch {
      case e: FileNotFoundException => "file not found"
      case _: Throwable => "unknow exception"
    }
    error should be("file not found")

    // Way to read large file, it is a buffered iterator, i.e. getLines() will
    // not read all content to memory, it is on-demand.
    val source = Source.fromFile("testdata/web.log")
    source.getLines.length should be(127)

    // test getLogLines
    val loglines1 = getLogLines("testdata/web.log")
    loglines1.length should be(127)
    val loglines2 = getLogLines("nofile")
    loglines2.length should be(0)

    // regex parsing
    val log = """64.242.88.10 - - [07/Mar/2004:16:35:19 -0800] "GET /mailman/listinfo/business HTTP/1.1" 200 6379"""
    val pattern = """^(\S+) (\S+) (\S+) \[(.*)\] "(.*)" (\S+) (\S+)""".r
    val parsedString = log match {
      case pattern(src, x1, x2, ts, action, code, byte) => s"$src, $x1, $x2, $ts, $action, $code, $byte"
      case pattern(_*) => "src matched only"
      case _ => "matched nothing"
    }
    parsedString should be("64.242.88.10, -, -, 07/Mar/2004:16:35:19 -0800, GET /mailman/listinfo/business HTTP/1.1, 200, 6379")

    // create a LogLine to simplify the content access
    val someOrNone = log match {
      case pattern(src, x1, x2, ts, action, code, byte) => Some(new LogLine(src, x1, x2, ts, action, code, byte))
      case pattern(_*) => None
    }
    someOrNone.isDefined should be(true)
    if (someOrNone.isDefined) {
      val logline: LogLine = someOrNone.get
      logline.code should be("200")
      logline.byte should be("6379")
    }
  }

  // Testing the Reservior
  @Test def reserviorTest = {
    var res = new Reservior(5)
    res.sample("2").sample("3").sample("1").sample("5").sample("4")
    res.array should be(Array("2","3","1","5","4"))
    res.sorted should be(Array("1","2","3","4","5"))

    res  = new Reservior(5)
    res.sample("2").sample("3").sample("1").sample("5")
    res.array should be(Array("2","3","1","5",null))
    res.sorted should be(Array("1","2","3","5"))  // smaller array

    res  = new Reservior(5)
    (1 to 100).foreach{ e => res.sample(e) }
    val sorted = res.sorted
    sorted.length should be(5)
    sorted should be(Array("19","27","40","42","99"))
  }

  /***************** Question #1
   * Result Codes (most frequent to least)
   */
  @Test def mostFreqResultCodesTest = {
    // Main logic
    val logLines = getLogLines("testdata/web.log")
    val freqCodes = logLines
                      .toList
                      .groupBy{ e => e.code }
                      .map{ case (k,v) => k -> v.size}
                      .toList.sortBy{ e => -e._2 }

    // Output
    println("\n-- Result Codes (most frequent to least) --")
    freqCodes.foreach{ e =>
      println(s"${e._1} - ${e._2} occurrences")
    }

    val output = freqCodes
                  .map{ e => s"${e._1} - ${e._2} occurrences"}
                  .mkString("\n")
    output should be(
"""200 - 93 occurrences
401 - 33 occurrences
404 - 1 occurrences""")
  }

  /***************** Question #2
   * Sources (most frequent to least)
   */
  @Test def mostFreqSourcesTest = {
    // Main logic
    val logLines = getLogLines("testdata/web.log")
    val freqCodes = logLines
                      .toList
                      .groupBy{ e => e.src }
                      .map{ case (k,v) => k -> v.size}
                      .toList.sortBy{ e => -e._2 }

    // Output
    println("\n-- Sources (most frequent to least) --")
    freqCodes.foreach{ e =>
      println(s"${e._1} - ${e._2} occurrences")
    }

    val output = freqCodes
                  .map{ e => s"${e._1} - ${e._2} occurrences"}
                  .mkString("\n")
    output should be(
"""64.242.88.10 - 116 occurrences
d207-6-9-183.bchsia.telus.net - 3 occurrences
80-219-148-207.dclient.hispeed.ch - 1 occurrences
lj1090.inktomisearch.com - 1 occurrences
mmscrm07-2.sac.overture.com - 1 occurrences
206-15-133-181.dialup.ziplink.net - 1 occurrences
lj1125.inktomisearch.com - 1 occurrences
h24-70-56-49.ca.shawcable.net - 1 occurrences
lordgun.org - 1 occurrences
lj1036.inktomisearch.com - 1 occurrences""")
  }

  /***************** Question #3
   * Bytes Distribution (percentiles)
   *
   * Using reservior sampling to estimate the percentiles.
   *
   * Below is using R to calculate the percentiles web.log
   *
   * > x = c(12846,4523,6291,7352,5253,11382,4924,12851,12851,3732,40520,12851,6379,46373,4140,3853,3686,12846,68,5724,5162,59679,12851,34395,7235,8545,6459,2869,4284,11400,12846,3675,5773,68,3860,12846,9310,808,5935,12846,11281,8806,12846,3848,4081,11281,4485,5234,3616,7771,23338,12846,12846,8820,6816,209,11314,12846,3095,3810,6948,12846,3376,3584,4548,6345,4449,12846,25416,4308,3544,12846,11284,27248,12846,5967,3596,12846,138789,3628,12851,52854,12851,6142,114241,12846,21162,4524,11444,12846,2937,9310,12846,12846,12846,11345,3838,7298,0,11266,46373,3826,20972,12846,7245,0,12846,12851,68,12846,12846,10118,12846,6738,7311,16670,5277,4982,12846,6294,14070,12846,12050,11281,40578,5846,300)
   * > quantile(x, c(.25, .50, .75))
   *   25%   50%   75%
   *  4504  8820 12846
   */
  @Test def byteDistributionTest = {
    // Main logic
    val res = new Reservior(100)
    val logLines = getLogLines("testdata/web.log")
    val freqCodes = logLines.foreach{ e => res.sample(e.byte) }
    val percentiles = res.sortedAsInt

    // Output
    println("\n-- Bytes Distribution (percentiles) --")
    println(s"25th Percentile: ${percentiles(24)}")
    println(s"50th Percentile: ${percentiles(49)}")
    println(s"75th Percentile: ${percentiles(74)}")

    percentiles(24) should be("4485")
    percentiles(49) should be("9310")
    percentiles(74) should be("12846")
  }
}
