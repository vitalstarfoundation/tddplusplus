package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class Test181101 extends FunSuite with BeforeAndAfterAll {
/* Restaurant (OOA - Requirement - What-Question)
class (concept)
instance (piece of document)
association (how one document leading to other document)
method (what you can do with the document)
inheritance (logical grouping)

OO only cares about the behavorial in the design
  - Encapsulation
*/

class Menu
class Dish {
  var customer: Customer = null
  var charge: Float = 0.0f
}
class Table
class Customer

class Order {
  var dish1: Dish = null
  var dish2: Dish = null
  var dish3: Dish = null
  var table: Table = null

  def addDish(dish: Dish) = {

    dish1 = dish
  }
}

class Check extends Order {
  def charge: Float = {
    val c1 = if (dish1 == null) 0 else dish1.charge
    val c2 = if (dish2 == null) 0 else dish2.charge
    val c3 = if (dish3 == null) 0 else dish3.charge
    c1 + c2 + c3
  }
}

    test("illustrate 5 OO principles") {
      val table1 = new Table
      val table2 = new Table
      val fryrice1 = new Dish
      val fryrice2 = new Dish

      val amour = new Customer
      val order = new Order
      order.table = table1  // association

      order.dish1 = fryrice1  // association
      order.addDish(fryrice1) // using a method

      val check = new Check  // inheritance
      check.addDish(fryrice1)

      //val check = new Order  // inheritance

      check.charge
    }

    test("composite pattern") {
    }
}
