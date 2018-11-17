package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

import scala.collection.mutable.ListBuffer
object TestClass1 {
  val objs = new ListBuffer[TestClass1]()
  val emptyobj = (new TestClass1("notfound"))
  emptyobj.setStr("empty")

  def createEmpty(id: String): TestClass1 = {
    val obj = new TestClass1(id)
    objs += obj
    obj
  }

  def findObject(id: String): TestClass1 = {
    objs.find{o => o.id == id}.getOrElse(emptyobj)
  }

  def list: List[String] = {
    objs.toList.map{ o => o.id }
  }
}

// @TODO Just a comment
case class TestClass1(id: String) {
  var str = "haha"

  def setText(s: String) = setStr(s)

  def setStr(s: String) = {str = s}
  override def toString(): String = {
    str
  }

  def asJSON: String = {
    s"$str"
  }

  def asHTML: String = {
    s"<h1>$str</h1>"
  }
}

@RunWith(classOf[JUnitRunner])
class TestTDD extends FunSuite with BeforeAndAfterAll {


  test("simple test") {

  }
}
