/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         testctx.scala
 * Last modified: 2012-10-15 20:40:58 EDT
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */
package com.github.salat.test

import com.github.salat._
import com.github.salat.test.model.Rhoda
import com.github.salat.util.encoding.TypeHintEncoding
import com.mongodb.casbah.Imports._

package object always {

  val ContextName = "TestContext-Always"

  implicit val ctx = new Context {
    val name = ContextName
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
  }
}

package object when_necessary {

  implicit val ctx = new Context {
    val name = "TestContext-WhenNecessary"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = TypeHint)
  }
}

package object never {
  implicit val ctx = new Context {
    val name = "TestContext-AlwaysTypeHints"
    override val typeHintStrategy = NeverTypeHint
  }
}

package object custom_type_hint {

  val CustomTypeHint = "_t"

  implicit val ctx = new Context {
    val name = "TestContext-Always"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = CustomTypeHint)
  }
}

package object always_with_implicits {

  implicit val ctx = new Context {
    val name = "TestContext-Always-Implicits"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
  }

  implicit def dbo2Obj[X <: CaseClass](obj: X): DBObject = ctx.lookup(obj.getClass.getName)
    .asInstanceOf[Grater[X]]
    .asDBObject(obj)

  // this requires ALWAYS using _typeHint, all the time!
  implicit def obj2MDbo[X <: CaseClass](dbo: MongoDBObject): X = ctx.lookup(dbo)
    .asInstanceOf[Grater[X]]
    .asObject(dbo)

  implicit def obj2Dbo[X <: CaseClass](dbo: DBObject): X = obj2MDbo(wrapDBObj(dbo))
}

package object per_class_key_remapping {
  implicit val ctx = new Context {
    val name = "TestContext-PerClassKeyRemapping"
  }
  ctx.registerPerClassKeyOverride(classOf[Rhoda], remapThis = "consumed", toThisInstead = "fire")
}

package object when_necessary_binary_type_hint_encoding {
  implicit val ctx = new Context {
    val name = "WhenNecessary-BinaryTypeHint"
    override val typeHintStrategy = BinaryTypeHintStrategy(
      when     = TypeHintFrequency.WhenNecessary,
      typeHint = "t",
      encoding = TypeHintEncoding.UsAsciiEncoding
    )
  }
}

package object always_binary_type_hint_encoding {
  implicit val ctx = new Context {
    val name = "Always-BinaryTypeHint"
    override val typeHintStrategy = BinaryTypeHintStrategy(
      when     = TypeHintFrequency.Always,
      typeHint = "t",
      encoding = TypeHintEncoding.UsAsciiEncoding
    )
  }
}

package object suppress_default_args {
  implicit val ctx = new Context {
    val name = "SuppressDefaultValues"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
    override val suppressDefaultArgs = true
  }
}

package object dont_suppress_default_args {
  implicit val ctx = new Context {
    val name = "DontSuppressDefaultValues"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
    override val suppressDefaultArgs = false
  }
}
