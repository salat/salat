/**
* @version $Id$
*/
package com.novus.salat.test

import org.specs.specification.PendingUntilFixed
import com.novus.salat.test.model.{Alice, Walrus}
import com.novus.salat._
import scala.tools.nsc.util.ScalaClassLoader
import com.mongodb.casbah.Imports._

class CustomContextSpec extends SalatSpec with PendingUntilFixed {

  "Salat context" should {

    "allow creation of a custom context" in {
      // create custom context
      val ctx = new Context {
        val name = Some("CustomContextSpec-1")
      }
      ctx.name must beSome("CustomContextSpec-1")
      // this custom context is using the default classloader
      ctx.classLoaders must haveSize(1)
      ctx.classLoaders(0) mustEqual ctx.getClass.getClassLoader

      // members of com.novus.salat.test.model can be resolved as expected
      ctx.classLoaders(0) mustEqual Alice.getClass.getClassLoader
      getClassNamed(Alice.getClass.getName)(ctx) must beSome(Alice.getClass)
    }

    "provide flexible classloader handling" in {

      val TestClassName = "com.novus.salat.test.CustomContextSpec$$anonfun$1$$anonfun$apply$10$$anon$2$Ida"
      val customCl: ClassLoader = new ScalaClassLoader() {
        case class Ida(lake: BigDecimal, drowned: Boolean)
        override def findClass(name: String): Class[_] = if (name == TestClassName) {
          log.info("CustomContextSpec: custom classloader returning %s for %s", classOf[Ida], name)
          classOf[Ida]
        }
        else throw new ClassNotFoundException
      }

      "allow registration of custom classloaders that precede the default classloader" in {
        val CustomContextName = "CustomContextSpec-2"
        val ctx = new Context {
          val name = Some(CustomContextName)
        }

        ctx.name must beSome(CustomContextName)
        ctx.classLoaders must haveSize(1)

        ctx.registerClassLoader(customCl)
        ctx.classLoaders must haveSize(2)

        val custom = ctx.classLoaders(0)
        val default = ctx.classLoaders(1)

        custom mustEqual customCl
        default mustEqual ctx.getClass.getClassLoader

        // we can resolve a class from the default classloader
        getClassNamed(Alice.getClass.getName)(ctx) must beSome(Alice.getClass)
        // we can resolve an imaginary text class from the custom classloader
        getClassNamed(TestClassName)(ctx) must beSome(Class.forName(TestClassName, true, customCl))

        // But from where?  Now try to force resolution from specific class loader and see what happens
        getClassNamed(Alice.getClass.getName)(new Context() {
          val name = Some("custom only")
          classLoaders = Seq(custom)
        }) must beNone
        getClassNamed(Alice.getClass.getName)(new Context() {
          val name = Some("default only")
          classLoaders = Seq(default)
        }) must beSome(Alice.getClass)
        getClassNamed(TestClassName)(new Context() {
          val name = Some("custom only")
          classLoaders = Seq(custom)
        }) must beSome(Class.forName(TestClassName, true, customCl))
        // well, with a full-on classloader mock, this might be possible - let's just settle for, it resolves in both classloaders
        // but by because custom precedes default it is obviously resolving from the custom
//        getClassNamed(TestClassName)(Seq(default)) must beNone
      }

      "allow creation of a context overrides defaults to specify its own classloader" in {
        val CustomContextName = "CustomContextSpec-3"
        val ctx = new Context {
          val name = Some(CustomContextName)
          classLoaders = Seq(customCl)

          override def registerClassLoader(cl: ClassLoader) = {
            log.info("This is my custom context and I would prefer not to register your classloader, sir")
            // do nothing
          }
        }

        ctx.name must beSome(CustomContextName)
        ctx.classLoaders must haveSize(1)
        ctx.classLoaders(0) mustEqual customCl
        ctx.classLoaders must notContain(ctx.getClass.getClassLoader)

        // try to register another classloader and confirm it didn't work, because we overrode the default impl
        val customCl2: ClassLoader = new ScalaClassLoader() {
          // some impl
        }
        ctx.registerClassLoader(customCl2)
        ctx.classLoaders must haveSize(1)
        ctx.classLoaders(0) mustEqual customCl
        ctx.classLoaders must notContain(customCl2)
        ctx.classLoaders must notContain(ctx.getClass.getClassLoader)

        // resolving a class from the default classloader doesn't work
        getClassNamed(Alice.getClass.getName)(ctx) must beNone
        // resolving a class from our custom classloader does
        getClassNamed(TestClassName)(ctx) must notBeEmpty
      }

      "percolate a custom context down the entire chain" in {
        val CustomContextName = "CustomContextSpec-4"
        implicit val ctx = new Context {
          val name = Some(CustomContextName)
        }

        ctx.name must beSome(CustomContextName)
        ctx.classLoaders must haveSize(1)

        ctx.registerClassLoader(customCl)
        ctx.classLoaders must haveSize(2)

        // we can resolve a class from the default classloader and get a working grater for it
        getClassNamed(Walrus.getClass.getName) must beSome(Walrus.getClass)
        val w = Walrus(Seq("a", "b", "C"))
        val dbo: MongoDBObject = grater[Walrus[String]].asDBObject(w)
        dbo.get("manyThings") must beSome[AnyRef]
        val w_* = grater[Walrus[String]].asObject(dbo)
        w_* mustEqual w

      }

    }


  }
}