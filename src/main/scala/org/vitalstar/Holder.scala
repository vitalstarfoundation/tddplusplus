package org.vitalstar

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}
import scala.util.Try

object Holder {
//  implicit val jsonFormats: Formats = DefaultFormats
  implicit val formats = DefaultFormats

  class EmptyHolder extends Holder {
    override def isEmpty = true
    override def size: Int = 0
    override def get(key: String): Holder = EmptyHolder
    override def find(key: String): Holder = EmptyHolder
    override def get(index: Int): Holder = EmptyHolder
    override def asString(default: String = ""): String = default
    override def asInt(default: Int = 0): Int = default
    override def asListHolder: Holder = EmptyHolder
  }

  val EmptyHolder = new EmptyHolder

  def apply(json: String): Holder = {
    val js: JValue = parse(json)
    new Holder(js)
  }

  def apply(value: JValue): Holder = {
    new Holder(value)
  }

  def readJsonHolder(path:String):Holder = {
    import scala.io.Source
    val configString = Source.fromFile(path).getLines.mkString
    Holder(configString)
  }

}

// By default Holder is holding a Map
class Holder {
//  implicit val jsonFormats: Formats = DefaultFormats
  implicit val formats = DefaultFormats

  var value: JValue = _

  protected def init(v:JValue) {value = v}
  def this(v: JValue) {
    this()
    init(v)
  }

  def isEmpty = false
    // not all JValue is a container.  Empty is only meaningful when it is
    // an object or list.  Therefore, by default any JValue is not empty

  def apply(key:String): Holder = get(key)
  def apply(index: Int): Holder = get(index)

  private def isValueEmpty(obj:JValue): Boolean = {
    if (obj.toOption.isEmpty) {
      true
    } else {
      // Try to detect JArray(List()) as empty return from JValue
      // This is a new 3.5.2 behavior.  In 3.2.11, it returns JNothing
      val opt = obj.extractOpt[List[Any]]
      if (opt.isEmpty){
        false
      } else {
        opt.get.isEmpty
      }
    }
  }

  private def _get(key:String, obj:JValue): Holder = {
    //if (obj.toOption.isEmpty) {
    if (isValueEmpty(obj)) {
      val opt = Try(key.toInt).toOption
      if (opt.isEmpty) {
        Holder.EmptyHolder
      } else {
        get(opt.get)
      }
    } else {
      Holder(obj)
    }
  }

  def get(key: String): Holder = {
    val obj = value \ key
    _get(key,obj)
  }

  def get(index: Int): Holder = {
    if (!isEmpty && (index >= 0 || index < value.children.size)) {
      val obj = value.children(index)
      if (obj.toOption.isEmpty) {
        Holder.EmptyHolder
      } else {
        Holder(obj)
      }
     } else {
       Holder.EmptyHolder
     }
  }

  //TODO: determine whether the key exists or is duplicate
  def find(key: String): Holder = {
    val obj = value \\ key  // JArray(List(...)) or JObject(List())
    // Because of obj will never by empty, so the key will never be convert
    // to Int to try get(index)
    _get(key,obj)
  }

  def path(key: String, delimiter:String = "\\."): Holder = {
    val keys = key.split(delimiter)
    keys.foldLeft(this){(holder,k) => holder.get(k)}
  }

  def asString(default: String = ""): String = {
    value.extractOrElse[String](default)
  }

  def asInt(default: Int = 0): Int = {
    value.extractOrElse[Int](default)
  }

  // Designed for use of case class.
  // Use other methods for accessing String, Int, List and Object.
  def as[T](default: T)(implicit mf: Manifest[T]): T = {
    value.extractOrElse[T](default)
  }

  def asListHolder(): Holder = {
     val opt = value.extractOpt[List[JValue]]
     if (opt.isDefined) {
       //new ListHolder(opt.get)  // this will create List but got converted
       new ListHolder(value)  // this will create JValue
     } else {
       Holder.EmptyHolder
     }
  }

  def size: Int = value.children.size

  def asJSONString = pretty(render(value))
  override def toString = s"""$size:\"${asString()}\""""
}

class ListHolder(lv: JValue) extends Holder(lv) {
  override def isEmpty = size == 0
  override def asString(default: String = ""): String = default
  override def asInt(default: Int = 0): Int = default
  override def asListHolder: Holder = this
}

