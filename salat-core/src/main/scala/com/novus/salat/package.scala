/**
 * Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/novus/salat
 *
 */
package com.novus

import com.mongodb.casbah.Imports._

import java.math.BigInteger
import com.novus.salat.{Grater, Context}
import com.mongodb.casbah.commons.Logging

package object salat extends Logging {

  type CaseClass = AnyRef with Product

  val TypeHint = "_typeHint"

  object TypeHintFrequency extends Enumeration {
    val Never, WhenNecessary, Always = Value
  }

  def timeAndLog[T](f: => T)(l: Long => Unit): T = {
    val t = System.currentTimeMillis
    val r = f
    l.apply(System.currentTimeMillis - t)
    r
  }

  def grater[X <: CaseClass](implicit ctx: Context, m: Manifest[X]): Grater[X] = ctx.lookup_![X](m)

  protected[salat] def getClassNamed(c: String)(implicit ctx: Context): Option[Class[_]] = {
//    log.info("getClassNamed(): looking for %s in %d classloaders", c, ctx.classLoaders.size)
    try {
      var clazz: Class[_] = null
//      var count = 0
      val iter = ctx.classLoaders.iterator
      while (clazz == null && iter.hasNext) {
        try {
          clazz = Class.forName(c, true, iter.next)
        }
        catch {
          case e: ClassNotFoundException => // keep going, maybe it's in the next one
        }

//        log.info("getClassNamed: %s %s in classloader '%s' %d of %d", c, (if (clazz != null) "FOUND" else "NOT FOUND"), ctx.name.getOrElse("N/A"), count, ctx.classLoaders.size)
//        count += 1
      }

      if (clazz != null) Some(clazz) else None
    }
    catch {
      case _ => None
    }
  }

  protected[salat] def getCaseClass(c: String)(implicit ctx: Context): Option[Class[CaseClass]] =
    getClassNamed(c).filter(_.getInterfaces.contains(classOf[Product])).map(_.asInstanceOf[Class[CaseClass]])

  implicit def shortenOID(oid: ObjectId) = new {
    def asShortString = (new BigInteger(oid.toString, 16)).toString(36)
  }

  implicit def explodeOID(oid: String) = new {
    def asObjectId = new ObjectId((new BigInteger(oid, 36)).toString(16))
  }

  implicit def class2companion(clazz: Class[_]) = new {
    def companionClass: Class[_] =
      Class.forName(if (clazz.getName.endsWith("$")) clazz.getName else "%s$".format(clazz.getName))

    def companionObject = companionClass.getField("MODULE$").get(null)
  }
}
