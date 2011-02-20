/**
 * @version $Id$
 */
package com.novus.salat

package object util {

  object ArgsPrettyPrinter {
    def apply(args: Seq[AnyRef]) = if (args == null) {
      "[NULL]"
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
    if (s != null && s.length > 100) s.substring(0, 100) + "..."  else s
  }
  else a
}