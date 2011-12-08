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

import com.novus.salat.test.model._
import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.GraterGlitch
import java.lang.reflect.Modifier

class ContextSpec extends SalatSpec {

  "The context classloader handling" should {
    "provide a classloader collection populated with its own classloader" in new testContext {
      ctx.classLoaders must contain(ctx.getClass.getClassLoader).only
    }
    "accept additional classloaders" in new testContext {
      ctx.classLoaders must have size (1)
      val cl = new ClassLoader() {}
      ctx.registerClassLoader(cl)
      ctx.classLoaders must have size (2)
      //      ctx.classLoaders must contain(cl, ctx.getClass.getClassLoader).only
    }
  }

  "Global field name overrides feature in the context" should {
    val remapThis = "id"
    val toThisInstead = "_id"
    "support registering a global key override" in new testContext {
      ctx.globalKeyOverrides must beEmpty
      ctx.registerGlobalKeyOverride(remapThis, toThisInstead)
      ctx.globalKeyOverrides.get(remapThis) must beSome(toThisInstead)
      ctx.globalKeyOverrides must have size (1)
    }
    "prevent registering a duplicate global override" in new testContext {
      ctx.globalKeyOverrides must beEmpty
      ctx.registerGlobalKeyOverride(remapThis, toThisInstead)
      ctx.registerGlobalKeyOverride(remapThis, toThisInstead) must throwA[java.lang.AssertionError]
    }
    "prevent registering a null or empty global override key" in new testContext {
      ctx.registerGlobalKeyOverride("", toThisInstead) must throwA[java.lang.AssertionError]
      ctx.registerGlobalKeyOverride(null, toThisInstead) must throwA[java.lang.AssertionError]
    }
    "prevent registering a null or empty global override value" in new testContext {
      ctx.registerGlobalKeyOverride(remapThis, "") must throwA[java.lang.AssertionError]
      ctx.registerGlobalKeyOverride(remapThis, null) must throwA[java.lang.AssertionError]
    }
  }

  "Per-class field name overrides in the context" should {
    val clazz = classOf[James]
    val remapThis = "id"
    val toThisInstead = "_id"
    "support registering a per-class key override" in new testContext {
      ctx.perClassKeyOverrides must beEmpty
      ctx.registerPerClassKeyOverride(clazz, remapThis, toThisInstead)
      ctx.perClassKeyOverrides.get(clazz.getName, remapThis) must beSome(toThisInstead)
      ctx.perClassKeyOverrides must have size (1)
    }
    "prevent registering a duplicate per-class override" in new testContext {
      ctx.perClassKeyOverrides must beEmpty
      ctx.registerPerClassKeyOverride(clazz, remapThis, toThisInstead)
      ctx.registerPerClassKeyOverride(clazz, remapThis, toThisInstead) must throwA[java.lang.AssertionError]
    }
    "prevent registering a null or empty per-class override key" in new testContext {
      ctx.registerPerClassKeyOverride(clazz, "", toThisInstead) must throwA[java.lang.AssertionError]
      ctx.registerPerClassKeyOverride(clazz, null, toThisInstead) must throwA[java.lang.AssertionError]
    }
    "prevent registering a null or empty per-class override value" in new testContext {
      ctx.registerPerClassKeyOverride(clazz, remapThis, "") must throwA[java.lang.AssertionError]
      ctx.registerPerClassKeyOverride(clazz, remapThis, null) must throwA[java.lang.AssertionError]
    }
  }

  "Using the context to determine field names" should {
    val clazz = classOf[James]
    val lye = "lye"
    val byMistake = "byMistake"
    val toThisInstead = "NaOH"
    "support global key overrides" in new testContext {
      ctx.determineFieldName(clazz, lye) must_== lye
      ctx.registerGlobalKeyOverride(lye, toThisInstead)
      ctx.determineFieldName(clazz, lye) must_== toThisInstead
    }
    "support per-class key overrides" in new testContext {
      ctx.determineFieldName(clazz, lye) must_== lye
      ctx.registerPerClassKeyOverride(clazz, lye, toThisInstead)
      ctx.determineFieldName(clazz, lye) must_== toThisInstead
    }
    "return field name unaffected when the context has no overrides for this field name" in new testContext {
      ctx.globalKeyOverrides must beEmpty
      ctx.perClassKeyOverrides must beEmpty
      ctx.determineFieldName(clazz, lye) must_== lye
      ctx.determineFieldName(clazz, byMistake) must_== byMistake
    }
  }

  "The context" should {
    "judge whether a class is suitable" in new testContext {
      "reject scala and java core classes" in {
        ctx.suitable_?("scala.X") must beFalse
        ctx.suitable_?("java.X") must beFalse
        ctx.suitable_?("javax.X") must beFalse
      }
      "allow concrete case classes" in {
        ctx.suitable_?(classOf[James].getName) must beTrue
      }
      "allow abstract classes, deferring actual check until concrete instance is provided" in {
        ctx.suitable_?((new MaudAgain {
          val swept = "swept"
          val out = "out"
        }).getClass.getName) must beTrue
      }
      "accept traits, deferring actual check until concrete instance is provided" in {
        ctx.suitable_?(new JamesLike {
          val lye = "lye"
        }.getClass.getName) must beTrue
      }
    }
  }

  "Using the context to lookup graters" should {
    //    "blow up when an unsuitable class is provided" in new testContext {
    //      ctx.lookup("some.nonexistent.clazz") must throwA[GraterGlitch]
    //    }
    "provide a lookup method that lazily generates and returns graters" in {
      "by class name" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(classOf[James].getName)
        //        g_* must beAnInstanceOf[Grater[_]]
        g_*.clazz.getName must_== (new ConcreteGrater[James](classOf[James]) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by case class manifest" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup[James]
        g_* must beAnInstanceOf[Grater[James]]
        g_*.clazz.getName must_== (new ConcreteGrater[James](classOf[James]) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by class name or instance of class" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(classOf[James].getName, James("Red Devil"))
        //        g_* must beAnInstanceOf[Grater[_]]
        g_*.clazz.getName must_== (new ConcreteGrater[James](classOf[James]) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by dbo with type hint" in new testContext {
        val dbo = MongoDBObject(TypeHint -> classOf[James].getName)
        ctx.graters must beEmpty
        val g_* = ctx.lookup(dbo)
        //        g_* must beAnInstanceOf[Grater[_]]
        g_*.clazz.getName must_== (new ConcreteGrater[James](classOf[James]) {}).clazz.getName
        ctx.graters must have size (1)
      }
    }

    "succeed for a case class" in new testContext {
      //      classOf[James] must beAnInstanceOf[CaseClass]
      ctx.lookup[James] must beAnInstanceOf[ConcreteGrater[James]]
    }
    "succeed for an abstract superclass" in new testContext {
      Modifier.isAbstract(classOf[Vertebrate].getModifiers) must beTrue
      val g = ctx.lookup(classOf[Vertebrate].getName)
      g.clazz.getName must_== classOf[Vertebrate].getName
      //      ctx.lookup(classOf[Vertebrate].getName) must haveSuperclass[Grater[_ <: AnyRef]]
      //      ctx.lookup(classOf[Vertebrate].getName) must haveClass[ProxyGrater[Vertebrate]]
    }
  }

  "The context" should {
    "accept a new grater" in new testContext {
      ctx.graters must beEmpty
      val grater = new ConcreteGrater[James](classOf[James]) {}
      ctx.accept(grater)
      ctx.graters.size must_== 1
      ctx.graters.get(James.getClass.getName.replace("$", "")) must beSome(grater)
    }
  }
}