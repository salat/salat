/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ContextSpec.scala
 * Last modified: 2012-10-15 20:40:59 EDT
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
package com.novus.salat.test

import com.novus.salat.test.model._
import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.GraterGlitch
import java.lang.reflect.Modifier
import org.specs2.specification.Scope

class ContextSpec extends SalatSpec {

  val caseClazz = classOf[James]
  val caseObjectClazz = Class.forName("com.novus.salat.test.model.Zoot$")
  val annotatedAbstractClazz = Class.forName("com.novus.salat.test.model.AbstractMaud")
  val abstractClazz = Class.forName("com.novus.salat.test.model.UnannotatedAbstractMaud")
  val annotatedTraitClazz = Class.forName("com.novus.salat.test.model.AnnotatedMaud")
  val traitClazz = Class.forName("com.novus.salat.test.model.UnannotatedMaud")

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
    implicit val ctx = new Context {
      val name = "test_context_%s".format(System.currentTimeMillis())
    }
    "judge whether a class is suitable" in {
      "reject scala and java core classes" in {
        ctx.suitable_?("scala.X") must beFalse
        ctx.suitable_?("java.X") must beFalse
        ctx.suitable_?("javax.X") must beFalse
      }
      "allow case objects" in {
        ctx.suitable_?(caseObjectClazz.getName) must beTrue
      }
      "allow concrete case classes" in {
        ctx.suitable_?(caseClazz.getName) must beTrue
      }
      "allow abstract classes, deferring actual check until concrete instance is provided" in {
        ctx.suitable_?(annotatedAbstractClazz.getName) must beTrue
        ctx.suitable_?(abstractClazz.getName) must beTrue
      }
      "allow traits, deferring actual check until concrete instance is provided" in {
        ctx.suitable_?(annotatedTraitClazz.getName) must beTrue
        ctx.suitable_?(traitClazz.getName) must beTrue
      }
    }
    "judge whether a class requires a proxy grater" in {
      "a case class does not require a proxy grater" in {
        ctx.needsProxyGrater(caseClazz) must beFalse
      }
      "a case object does not require a proxy grater" in {
        ctx.needsProxyGrater(caseObjectClazz) must beFalse
      }
      "an abstract class annotated with @Salat requires a proxy grater" in {
        ctx.needsProxyGrater(annotatedAbstractClazz) must beTrue
      }
      "an abstract class without @Salat annotation requires a proxy grater" in {
        ctx.needsProxyGrater(abstractClazz) must beTrue
      }
      "a trait annotated with @Salat requires a proxy grater" in {
        ctx.needsProxyGrater(annotatedTraitClazz) must beTrue
      }
      "a trait that is not annotated with @Salat requires a proxy grater" in {
        ctx.needsProxyGrater(traitClazz) must beTrue
      }
    }
    "offer a feasibility lookup method for graters" in {
      "must return Some for a case class" in {
        ctx.lookup_?(caseClazz.getName) must beSome[Grater[_]]
      }
      "must return None for a case object" in {
        ctx.lookup_?(caseObjectClazz.getName) must beSome[Grater[_]]
      }
      "must return Some for an abstract class annotated with @Salat" in {
        ctx.lookup_?(annotatedAbstractClazz.getName) must beSome[Grater[_]]
      }
      "must return Some for an abstract class without @Salat annotation" in {
        ctx.lookup_?(abstractClazz.getName) must beSome[Grater[_]]
      }
      "must return Some for a trait annotated with @Salat" in {
        ctx.lookup_?(annotatedTraitClazz.getName) must beSome[Grater[_]]
      }
      "must return Some for a trait without @Salat annotation" in {
        ctx.lookup_?(traitClazz.getName) must beSome[Grater[_]]
      }
    }
    "extract type hints intelligently" in {
      "allow case class type hint" in {
        ctx.extractTypeHint(MongoDBObject(TypeHint -> caseClazz.getName)) must beSome(caseClazz.getName)
      }
      "allow case object type hint" in {
        ctx.extractTypeHint(MongoDBObject(TypeHint -> caseObjectClazz.getName)) must beSome(caseObjectClazz.getName)
      }
      "filter out abstract class annotated with @Salat" in {
        ctx.extractTypeHint(MongoDBObject(TypeHint -> annotatedAbstractClazz.getName)) must beNone
      }
      "filter out abstract class without @Salat annotation" in {
        ctx.extractTypeHint(MongoDBObject(TypeHint -> abstractClazz.getName)) must beNone
      }
      "filter out trait annotated with @Salat" in {
        ctx.extractTypeHint(MongoDBObject(TypeHint -> annotatedTraitClazz.getName)) must beNone
      }
      "filter out trait without @Salat annotation" in {
        ctx.extractTypeHint(MongoDBObject(TypeHint -> traitClazz.getName)) must beNone
      }
    }
  }

  "Using the context to lookup graters" should {
    //    "blow up when an unsuitable class is provided" in new testContext {
    //      ctx.lookup("some.nonexistent.clazz") must throwA[GraterGlitch]
    //    }
    "provide a lookup method that lazily generates and returns graters" in {
      "by class name for a case class" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(caseClazz.getName)
        g_*.clazz.getName must_== (new ConcreteGrater[James](caseClazz)(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by class name for a case object" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(caseObjectClazz.getName)
        log.debug(g_*.toString)
        // TODO: can't get type for com.novus.salat.test.model.Zoot$
        //        g_*.clazz.getName must_== (new ProxyGrater[Zoot](caseObjectClazz)(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by class name for an abstract class annotated with @Salat" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(annotatedAbstractClazz.getName)
        g_*.clazz.getName must_== (new ProxyGrater[AbstractMaud](
          annotatedAbstractClazz.asInstanceOf[Class[AbstractMaud]])(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by class name for an abstract class without @Salat annotation" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(abstractClazz.getName)
        g_*.clazz.getName must_== (new ProxyGrater[UnannotatedAbstractMaud](
          abstractClazz.asInstanceOf[Class[UnannotatedAbstractMaud]])(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by class name for a trait annotated with @Salat" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(annotatedTraitClazz.getName)
        g_*.clazz.getName must_== (new ProxyGrater[AnnotatedMaud](
          annotatedTraitClazz.asInstanceOf[Class[AnnotatedMaud]])(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by class name for a trait without @Salat annotation" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(traitClazz.getName)
        g_*.clazz.getName must_== (new ProxyGrater[UnannotatedMaud](
          traitClazz.asInstanceOf[Class[UnannotatedMaud]])(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by case class manifest" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup[James]
        g_* must beAnInstanceOf[Grater[James]]
        g_*.clazz.getName must_== (new ConcreteGrater[James](classOf[James])(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by class name for instance of a case class" in new testContext {
        ctx.graters must beEmpty
        val g_* = ctx.lookup(classOf[James].getName, James("Red Devil"))
        //        g_* must beAnInstanceOf[Grater[_]]
        g_*.clazz.getName must_== (new ConcreteGrater[James](classOf[James])(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
      "by dbo with type hint" in new testContext {
        val dbo = MongoDBObject(TypeHint -> classOf[James].getName)
        ctx.graters must beEmpty
        val g_* = ctx.lookup(dbo)
        //        g_* must beAnInstanceOf[Grater[_]]
        g_*.clazz.getName must_== (new ConcreteGrater[James](classOf[James])(ctx) {}).clazz.getName
        ctx.graters must have size (1)
      }
    }

    "succeed for a case class" in new testContext {
      //      classOf[James] must beAnInstanceOf[CaseClass]
      ctx.lookup[James] must beAnInstanceOf[ConcreteGrater[James]]
    }
    //    "succeed for an abstract superclass" in new testContext {
    //      Modifier.isAbstract(classOf[Vertebrate].getModifiers) must beTrue
    //      val g = ctx.lookup(classOf[Vertebrate].getName)
    //      g.clazz.getName must_== classOf[Vertebrate].getName
    //      //      ctx.lookup(classOf[Vertebrate].getName) must haveSuperclass[Grater[_ <: AnyRef]]
    //      //      ctx.lookup(classOf[Vertebrate].getName) must haveClass[ProxyGrater[Vertebrate]]
    //    }
  }

  "The context" should {
    "accept a new grater" in new testContext {
      ctx.graters must beEmpty
      val grater = new ConcreteGrater[James](classOf[James])(ctx) {}
      ctx.accept(grater)
      ctx.graters.size must_== 1
      ctx.graters.get(James.getClass.getName.replace("$", "")) must beSome(grater)
    }
  }

  "The context numeric strategy for BigDecimal" should {
    "provide BigDecimal <-> Double support" in new customBigDecimalCtx(BigDecimalToDoubleStrategy()) {
      val out = ctx.bigDecimalStrategy.out(x)
      out must_== 3.14d
      ctx.bigDecimalStrategy.in(out) must_== x
    }
    "provide BigDecimal <-> String support" in new customBigDecimalCtx(BigDecimalToStringStrategy()) {
      val out = ctx.bigDecimalStrategy.out(x)
      out must_== "3.14"
      ctx.bigDecimalStrategy.in(out) must_== x
    }
    "provide BigDecimal <-> Binary support" in new customBigDecimalCtx(BigDecimalToBinaryStrategy()) {
      val out = ctx.bigDecimalStrategy.out(x)
      out must_== "3.14".toCharArray.map(_.asInstanceOf[Byte])
      ctx.bigDecimalStrategy.in(out) must_== x
    }
  }

  //  "The context numeric for BigInt" should {
  //    "provide "
  //  }

  "The context" should {
    "support suppressing default args" in {
      import com.novus.salat.test.model.suppress_default_args._
      import com.novus.salat.test.model.sda._
      grater[Bar].asDBObject(Bar()) must beEmpty
    }
  }
}