/**
 * @version $Id$
 */
package com.novus.salat.test.dao

import com.novus.salat._
import com.novus.salat.test._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._


case class Foo(x: Int, y: String)

object FooDAO extends com.novus.salat.dao.SalatDAO[Foo] {

  val _grater = grater[Foo]

  val collection = MongoConnection()(SalatSpecDb)("foo-dao-spec")

}