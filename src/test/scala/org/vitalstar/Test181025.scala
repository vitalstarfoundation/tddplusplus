package org.vitalstar

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._
import org.junit.runner.RunWith

// OOA Concepts (WHAT, Requirement)
class aReceiver
  // aReceiver is the generalization of aSender and aOwner
class aSender extends aReceiver

class aContact
class aMessage
class aGroup
class aOwner
class aNotification

//Use Case - Login & Logout
/*
aOwner.login(“my id”, “my password”) // how many retries allowed?
aOwner.updatePassword(“old password”,”new password”)

// Forget password
aOwner.lostPassword(“my id”)
aOwner.lostId(“my@email.com”)
aOwner.resetPassword(“lohjfoihjweof;j;sofj;oj”, “new password”, “confirm password)
 */

@RunWith(classOf[JUnitRunner])
class Test181025 extends FunSuite with BeforeAndAfterAll {
  // OOD -  (HOW, Design)
  // UC: Set owner info
  // UC: Erase owner info
  class DeviceOwner {
    var m_name: String = "Unknown"
    def setName(name: String) = m_name = name
    override def toString: String = m_name
  }
  class IOSDevice {
    var m_owner: DeviceOwner = null

    // Singleton
    def owner: DeviceOwner = {
      if (m_owner == null) {
        m_owner = new DeviceOwner
      }
      m_owner
    }

    def setName(name: String) = {
      if (m_owner != null) {
        m_owner.setName(name)
      }
    }
    override def toString: String = {
      if (m_owner == null) {
        "I have no owner"
      } else {
        s"${m_owner.toString}'s IOS Device"
      }
    }
  }

  val iphone = new IOSDevice
//  iphone.setName("Amour")

  val ipad = new IOSDevice
  val appleTV = new IOSDevice
  val ipod = new IOSDevice

  test("simple test") {
    val x = 123
    assertEquals(124, x + 1)
    assertFalse(124==x + 2)
  }

  test("test owner unique") {
    // UC: IOS Device should have an owner
    assertNotNull(iphone.owner)

    // UC: IOS Device create an unique owner
    val o1 = iphone.owner
    val o2 = iphone.owner
    assertSame(o1, o2)

    // UC: IOS Device can set owner name
    assertNotNull(iphone.toString())
    iphone.setName("Amour")
    assertEquals("Amour's IOS Device", iphone.toString())
    assertEquals("Amour", iphone.owner.toString())

    // UC: IOS Device should set the device serial number

    // UC: blah blah blah


  }





}
