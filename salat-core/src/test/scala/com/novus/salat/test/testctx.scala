/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.test

import java.math.{ MathContext, RoundingMode }
import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.novus.salat.test.model.Rhoda
import com.novus.salat.util.encoding.TypeHintEncoding
import com.mongodb.casbah.Imports

package object always {

  implicit val ctx = new Context {
    val name = Some("TestContext-Always")
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
  }
}

package object when_necessary {

  implicit val ctx = new Context {
    val name = Some("TestContext-WhenNecessary")
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = TypeHint)
  }
}

package object never {
  implicit val ctx = new Context {
    val name = Some("TestContext-AlwaysTypeHints")
    override val typeHintStrategy = NeverTypeHint
  }
}

package object custom_type_hint {

  val CustomTypeHint = "_t"

  implicit val ctx = new Context {
    val name = Some("TestContext-Always")
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = CustomTypeHint)
  }
}

package object always_with_implicits {

  implicit val ctx = new Context {
    val name = Some("TestContext-Always-Implicits")
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
  }

  implicit def dbo2Obj[X <: CaseClass](obj: X): DBObject = ctx.lookup_!(obj.getClass.getName)
    .asInstanceOf[Grater[X]]
    .asDBObject(obj)

  // this requires ALWAYS using _typeHint, all the time!
  implicit def obj2MDbo[X <: CaseClass](dbo: MongoDBObject): X = ctx.lookup_!(dbo)
    .asInstanceOf[Grater[X]]
    .asObject(dbo)
  implicit def obj2Dbo[X <: CaseClass](dbo: DBObject): X = obj2MDbo(wrapDBObj(dbo))
}

package object per_class_key_remapping {
  implicit val ctx = new Context {
    val name = Some("TestContext-PerClassKeyRemapping")
  }
  ctx.registerPerClassKeyOverride(classOf[Rhoda], remapThis = "consumed", toThisInstead = "fire")
}

package object when_necessary_binary_type_hint_encoding {
  implicit val ctx = new Context {
    val name = Some("WhenNecessary-BinaryTypeHint")
    override val typeHintStrategy = BinaryTypeHintStrategy(when = TypeHintFrequency.WhenNecessary,
      typeHint = "t",
      encoding = TypeHintEncoding.UsAsciiEncoding)
  }
}

package object always_binary_type_hint_encoding {
  implicit val ctx = new Context {
    val name = Some("Always-BinaryTypeHint")
    override val typeHintStrategy = BinaryTypeHintStrategy(when = TypeHintFrequency.Always,
      typeHint = "t",
      encoding = TypeHintEncoding.UsAsciiEncoding)
  }
}