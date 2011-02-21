/**
 * @version $Id$
 */
package com.novus.salat

import com.mongodb.casbah.Imports._

package object util {

  val NullPlaceholder = "[NULL]"

  object ArgsPrettyPrinter {
    def apply(args: Seq[AnyRef]) = if (args == null) {
      NullPlaceholder
    }
    else {
      val builder = Seq.newBuilder[String]
      val p = "[%d]\t%s\n\t\t%s"
      for (i <- 0 until args.length) {
        val o = args(i)
        builder += p.format(i, o.getClass, truncate(o))
      }
      builder.result.mkString("\n")
    }
  }

  def truncate(a: AnyRef) = if (a == null) {
    val s = a.toString
    if (s != null && s.length > 100) s.substring(0, 100) + "..." else s
  }
  else a

  def convertBasicDBObject(m: BasicDBObject): Map[AnyRef, AnyRef] = if (m == null) {
    Map.empty[AnyRef, AnyRef]
  }
  else {
    val builder = Map.newBuilder[AnyRef, AnyRef]
      for ((k, v) <- m) {
        builder += k -> v
      }
    builder.result
  }

  object MapPrettyPrinter {
    def apply(m: MongoDBObject): String = apply(m.toMap[AnyRef, AnyRef])
    def apply(m: BasicDBObject): String = apply(convertBasicDBObject(m))

    def apply[A<:Any,B<:Any](m: Map[A, B], limit: Int = 10) = if (m == null) {
      NullPlaceholder
    }
    else {
      val builder = Seq.newBuilder[String]
      val kv = "[%d] k=%s\tv=%s\n\t%s -> %s"
      var counter = 0
      val iter = m.iterator
      while (iter.hasNext && counter < limit) {
        val (k, v) = {
          val (k_any, v_any) = iter.next
          (k_any.asInstanceOf[AnyRef], v_any.asInstanceOf[AnyRef])
        }
        builder += kv.format(counter, k.getClass, v.getClass, truncate(k), truncate(v))
        counter += 1
      }
      builder.result.mkString("\n")
    }
  }

}