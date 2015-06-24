/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         MissingGraterExplanation.scala
 * Last modified: 2012-12-06 22:48:17 EST
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
package com.github.salat.util

import java.lang.reflect.Modifier

import com.github.salat._
import com.mongodb.casbah.commons.Imports._

object MissingGraterExplanation extends Logging {

  def clazzFromTypeHint(typeHint: Option[String])(implicit ctx: Context): Option[Class[_]] = typeHint match {
    case Some(th) => try {
      getClassNamed(th)
    }
    catch {
      case c: ClassNotFoundException => None
      case e: Throwable => {
        log.error("Type hint from dbo with key='%s', value='%s' is some bad candy!".format(ctx.typeHintStrategy.typeHint, th), e)
        None
      }
    }
    case _ => None
  }

  def clazzFromPath(path: String)(implicit ctx: Context): Option[Class[_]] = if (path != null) {
    try {
      getClassNamed(path)
    }
    catch {
      case c: ClassNotFoundException => None
      case e: Throwable => {
        log.error("Error resolving class from path='%s'".format(path), e)
        None
      }
    }
  }
  else None

  def getReasonForPathClazz(pathClazz: Option[Class[_]], path: String): String = {
    pathClazz match {
      case Some(clazz) if clazz.getEnclosingClass != null         => "Class %s has enclosing class %s".format(clazz.getName, clazz.getEnclosingClass.getName)
      case Some(clazz) if clazz.isInstanceOf[CaseClass]           => "Well, this is a case class, so... not sure what went wrong."
      case Some(clazz) if clazz.isInterface                       => "Class %s is an interface".format(clazz.getName)
      case Some(clazz) if Modifier.isAbstract(clazz.getModifiers) => "Class %s is abstract".format(clazz.getName)
      case Some(clazz) if !clazz.isInstanceOf[CaseClass]          => "Class %s is not an instance of CaseClass".format(clazz.getName)
      case None if path != null && pathClazz.isEmpty => "Very strange!  Path='%s' from pickled ScalaSig causes ClassNotFoundException".
        format(path)
      case _ => "Unable to determine why grater was not found"
    }
  }

  def apply(path: String)(implicit ctx: Context) = {
    val pathClazz = clazzFromPath(path)
    val reason = getReasonForPathClazz(pathClazz, path)

    val explanation = """

      GRATER GLITCH - unable to find or instantiate a grater using supplied path name

      REASON: %s

      Context: '%s'
      Path from pickled Scala sig: '%s'


       """.format(reason, ctx.name, path)

    explanation
  }

  def apply(path: String, dbo: MongoDBObject)(implicit ctx: Context) = {

    val typeHint = ctx.extractTypeHint(dbo)
    val hintClazz = clazzFromTypeHint(typeHint)(ctx)
    val pathClazz = clazzFromPath(path)
    val reason = hintClazz match {
      case Some(clazz) if clazz.getEnclosingClass != null         => "Class %s has enclosing class %s".format(clazz.getName, clazz.getEnclosingClass.getName)
      case Some(clazz) if clazz.isInstanceOf[CaseClass]           => "Well, this is a case class, so... not sure what went wrong."
      case Some(clazz) if clazz.isInterface                       => "Class %s is an interface".format(clazz.getName)
      case Some(clazz) if Modifier.isAbstract(clazz.getModifiers) => "Class %s is abstract".format(clazz.getName)
      case Some(clazz) if !clazz.isInstanceOf[CaseClass]          => "Class %s is not an instance of CaseClass".format(clazz.getName)
      case None if typeHint.isDefined && hintClazz.isEmpty => "Type hint %s='%s' causes ClassNotFoundException".
        format(ctx.typeHintStrategy.typeHint, typeHint)
      case None if path == null && typeHint.isEmpty => "Unknown class: type hint is empty and path from pickled ScalaSig is null"
      case _                                        => getReasonForPathClazz(pathClazz, path)
    }

    val explanation = """

      GRATER GLITCH - unable to find or instantiate a grater using supplied DBO type hint and/or path name

      REASON: %s

      Context: '%s'
      Path from pickled Scala sig: '%s'
      DBO type hint: '%s'

      FELL DOWN DESERIALZIING:
      %s

       """.format(reason, ctx.name, path, typeHint.getOrElse(""), dbo)

    explanation
  }
}
