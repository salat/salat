/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         PrettyPrinters.scala
 * Last modified: 2012-12-06 22:32:50 EST
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
package com.novus.salat.util

import java.lang.reflect.{ParameterizedType, Type}

import com.novus.salat.{ConcreteGrater, DefaultArg, Field => SField}

import scala.reflect.Manifest
import scala.tools.scalap.scalax.rules.scalasig.TypeRefType

object FieldPrettyPrinter {
  // scala transliteration of java.lang.reflect.Field.getTypeName 
  // (which is, curses, scoped package local to java.lang.reflect)
  def apply(t: Type): String = t match {
    case clazz: Class[_] => apply0(clazz)
    case x               => x.toString
  }

  def apply0(t: Class[_]): String = {
    if (t.isArray) {
      try {
        var cl = t
        var dimensions = 0
        while (cl.isArray) {
          dimensions += 1
          cl = cl.getComponentType
        }
        val builder = List.newBuilder[String]
        builder += cl.getName
        for (i <- 0 until dimensions) {
          builder += "[]"
        }
        builder.result().mkString("")
      }
      catch {
        case e: Throwable => "" // do nothing
      }
    }
    else t.getName
  }
}

trait GraterPrettyPrinter {
  def getField[X <: AnyRef with Product](indexedFields: Seq[SField], arity: Int): Option[SField] = {
    indexedFields.find(_.idx == arity)
  }

  def fieldName[X <: AnyRef with Product](field: Option[SField]): String = {
    field.map(_.name).getOrElse(EmptyPlaceholder)
  }

  def safeDefault[X <: AnyRef with Product](field: Option[SField], defaultArgs: Map[SField, DefaultArg]): String = {
    field.flatMap(defaultArgs.get(_)).
      flatMap(_.value.map(_.toString)).
      getOrElse(OptionalMissingPlaceholder)
  }

  def ignore[X <: AnyRef with Product](field: Option[SField]): String = {
    field.map(_.ignore.toString).getOrElse(QuestionPlaceholder)
  }
}

object ConstructorPrettyPrinter extends GraterPrettyPrinter {
  def apply[X <: AnyRef with Product](g: ConcreteGrater[X]) = {
    val indexedFields = g.indexedFields
    val constructor = g.ca.constructor
    val defaultArgs = g.betterDefaults

    val builder = List.newBuilder[String]
    builder += "CONSTRUCTOR\n"
    builder += constructor.toGenericString
    var arity = 0
    val p = "PARAM\t[%d]\nNAME\t%s\nTYPE\t%s\nDEFAULT ARG\t%s\n@Ignore\t%s\n"
    for (param <- constructor.getGenericParameterTypes) {
      val field = getField(indexedFields, arity)
      builder += p.format(
        arity,
        fieldName(field),
        FieldPrettyPrinter(param),
        safeDefault(field, defaultArgs),
        ignore(field)
      )
      arity += 1
    }

    builder.result().mkString("\n")
  }
}

object ConstructorInputPrettyPrinter extends GraterPrettyPrinter with Logging {
  def apply[X <: AnyRef with Product](g: ConcreteGrater[X], args: Seq[AnyRef]) = {
    val indexedFields = g.indexedFields
    val constructor = g.ca.constructor
    val defaultArgs = g.betterDefaults

    val builder = List.newBuilder[String]
    builder += "CONSTRUCTOR"
    builder += constructor.toGenericString
    val p = """
---------- CONSTRUCTOR EXPECTS FOR PARAM [%d] --------------
NAME:         %s
TYPE:	      %s
DEFAULT ARG   %s
@Ignore	      %s
---------- CONSTRUCTOR INPUT ------------------------
%s
------------------------------------------------------------
    """
    var arity = 0
    for (param <- constructor.getGenericParameterTypes) {
      val field = getField(indexedFields, arity)
      val arg = if (args.isDefinedAt(arity)) Option(args(arity)) else None
      builder += p.format(
        arity,
        fieldName(field),
        FieldPrettyPrinter(param),
        safeDefault(field, defaultArgs),
        ignore(field),
        //        (if (ClassPrettyPrinter(param) == ClassPrettyPrinter(arg)) "GOOD" else "BAD"),
        ArgPrettyPrinter(arg)
      )
      arity += 1
    }

    if (args.length > arity) {
      log.debug("\nARITY: %d\nARGS: %d", arity, args.length)
      for (i <- arity until args.length) {
        log.debug("i: %d", i)
        val arg = args(i)
        builder += """
---------- UNEXPECTED EXTRA PARAM [%d] ---------------------
%s
------------------------------------------------------------
             """.format(i, ArgPrettyPrinter(arg))
      }
    }

    builder.result().mkString("\n")
  }
}

object ArgPrettyPrinter {

  def apply(arg: Option[AnyRef]): String = arg.map(apply _).getOrElse(MissingPlaceholder)

  def apply(arg: AnyRef): String = if (arg == null) {
    NullPlaceholder
  }
  else {
    // TODO: my kingdom for an HList - the AnyRef bit squashes all the types to Object
    val builder = List.newBuilder[String]
    builder += "TYPE: %s".format(ClassPrettyPrinter(arg))
    builder += "VALUE:\n%s".format(truncate(arg))
    builder.result().mkString("\n")
    //    truncate(arg)
  }
}

object ClassPrettyPrinter {

  // pattern matching on Any causes scala to hang indefinitely

  //  def apply(o: Any): String = o. match {
  //    case Byte    => Manifest.Byte.toString
  //    case Short   => Manifest.Short.toString
  //    case Char    => Manifest.Char.toString
  //    case Int     => Manifest.Int.toString
  //    case Long    => Manifest.Long.toString
  //    case Float   => Manifest.Float.toString
  //    case Double  => Manifest.Int.toString
  //    case Boolean => Manifest.Boolean.toString
  //    case Unit    => Manifest.Unit.toString
  //    case x       => apply(x.isInstanceOf[AnyRef]) // squish
  //  }

  def apply[X <: AnyRef: Manifest](o: X): String = o match {
    //    case None                               => NonePlaceholder
    case null => NullPlaceholder
    //    case x: TraversableOnce[_] if x.isEmpty => EmptyPlaceholder
    //    case x: Map[_, _] if x.isEmpty          => EmptyPlaceholder
    case _ => manifest[X].toString match {
      case "Object" => {
        // bah humbug
        // didn't get a usable manifest, probably as the result of a collection
        // squishing AnyRef - fall back to getting the class of the instance
        val clazz = o.getClass
        val typeParams = {
          // TODO: reify generics
          val p = clazz.getGenericSuperclass match {
            case pt: ParameterizedType => pt.getActualTypeArguments.map(FieldPrettyPrinter(_))
            case _                     => clazz.getTypeParameters
          }
          p.toList match {
            case Nil  => ""
            case list => list.mkString("[", ", ", "]")
          }

        }
        clazz.getName + typeParams
      }
      case m => m
    }
  }
}

object ArgsPrettyPrinter {
  def apply(args: Seq[AnyRef]) = if (args == null) {
    NullPlaceholder
  }
  else if (args.isEmpty) {
    EmptyPlaceholder
  }
  else {
    val builder = Seq.newBuilder[String]
    val p = "[%d]\t%s\n\t\t%s"
    for (i <- 0 until args.length) {
      val o = args(i)
      builder += p.format(i, o.getClass, truncate(o))
    }
    builder.result().mkString("\n")
  }
}

object TransformPrettyPrinter {

  def apply(which: String, value: Any, t: TypeRefType, xformed: Any): String = {
    """
    %s:
    [BEFORE] %s:     '%s'
        --[%s]-->
    [AFTER] %s:     '%s'
    """.format(
      which,
      ClassPrettyPrinter(value.asInstanceOf[AnyRef]),
      value,
      t.symbol.path,
      ClassPrettyPrinter(xformed.asInstanceOf[AnyRef]),
      xformed
    )
  }
}

