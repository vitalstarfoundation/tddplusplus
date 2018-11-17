package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class Test181108 extends FunSuite with BeforeAndAfterAll {
/*
customerID,gender,SeniorCitizen,Partner,Dependents,tenure,PhoneService,MultipleLines,InternetService,OnlineSecurity,OnlineBackup,DeviceProtection,TechSupport,StreamingTV,StreamingMovies,Contract,PaperlessBilling,PaymentMethod,MonthlyCharges,TotalCharges,Churn
7590-VHVEG,Female,0,Yes,No,1,No,No phone service,DSL,No,Yes,No,No,No,No,Month-to-month,Yes,Electronic check,29.85,29.85,No
5575-GNVDE,Male,0,No,No,34,Yes,No,DSL,Yes,No,Yes,No,No,No,One year,No,Mailed check,56.95,1889.5,No
3668-QPYBK,Male,0,No,No,2,Yes,No,DSL,Yes,Yes,No,No,No,No,Month-to-month,Yes,Mailed check,53.85,108.15,Yes
7795-CFOCW,Male,0,No,No,45,No,No phone service,DSL,Yes,No,Yes,Yes,No,No,One year,No,Bank transfer (automatic),42.3,1840.75,No
9237-HQITU,Female,0,No,No,2,Yes,No,Fiber optic,No,No,No,No,No,No,Month-to-month,Yes,Electronic check,70.7,151.65,Yes
9305-CDSKC,Female,0,No,No,8,Yes,Yes,Fiber optic,No,No,Yes,No,Yes,Yes,Month-to-month,Yes,Electronic check,99.65,820.5,Yes
1452-KIOVK,Male,0,No,Yes,22,Yes,Yes,Fiber optic,No,Yes,No,No,Yes,No,Month-to-month,Yes,Credit card (automatic),89.1,1949.4,No
6713-OKOMC,Female,0,No,No,10,No,No phone service,DSL,Yes,No,No,No,No,No,Month-to-month,No,Mailed check,29.75,301.9,No

testdata/test_data.csv

val s = Source.fromFile("testdata/test_data.csv")
val lines = s.getLines.toList

val csv = new CSVFile("testdata/test_data.csv")
csv.getRow

 * */

class CSVFile {
  import scala.io.Source
  var lines: List[String] = null

  def this(filename: String) = {
    this()
    val source = Source.fromFile(filename)
    lines = source.getLines.toList
  }

  def getHeader: Row = new Row(lines(0))

  def getAllRows: List[Row] = {
    lines.drop(1).map{ e => new Row(e) }
  }
}

class Row() {
  var row: Array[String] = Array()

  def this(text: String) = {
    this()
    row = text.trim.split(",")
  }

  def get(name: String) : String = {
    val fieldname = "customerID,gender,SeniorCitizen,Partner,Dependents,tenure,PhoneService,MultipleLines,InternetService,OnlineSecurity,OnlineBackup,DeviceProtection,TechSupport,StreamingTV,StreamingMovies,Contract,PaperlessBilling,PaymentMethod,MonthlyCharges,TotalCharges,Churn"
    val fields = fieldname.trim.split(",")
    val i = fields.indexOf(name)
    row(i)
  }

  override def toString(): String = row.mkString(",")
}


  test("smoke test") {
    val l = "7590-VHVEG,Female,0,Yes,No,1,No,No phone service,DSL,No,Yes,No,No,No,No,Month-to-month,Yes,Electronic check,29.85,29.85,No"
    val w = l.trim.split(",")
  }

  test("scala slice") {
    // 1. show how slice works
    // 2. Functional is creating new by copying from the old
    assertEquals("Dan", "D" + "man".slice(1,3))
  }

  test("get field from a row") {
    val r = new Row("7590-VHVEG,Female,0,Yes,No,1,No,No phone service,DSL,No,Yes,No,No,No,No,Month-to-month,Yes,Electronic check,29.85,29.85,No")
    assertEquals("Female", r.get("gender"))
  }

  test("all charges") {
    val csv = new CSVFile("testdata/test_data.csv")
    val sum = csv.getAllRows
              .map{ e => e.get("MonthlyCharges").toFloat }
              .sum
    assertEquals(456117.56, sum, 1e-1)
  }
}
