/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-util
 * Class:         glitch.scala
 * Last modified: 2012-08-08 13:27:16 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */

package com.github.salat.util

import java.lang.reflect.Constructor
// a useful place to be when things go pear-shaped
// p.s. could more people throw exceptions like these?

/**
 * Runtime error indicating that a class defines more than one constructor with args.
 *  @param clazz parameterized class instance
 *  @param cl list of parameterized constructors found for this class
 *  @tparam X any reft
 */
case class TooManyConstructorsWithArgs[X](clazz: Class[X], cl: List[Constructor[X]]) extends Error(
  "constructor: clazz=%s ---> expected 1 constructor with args but found %d\n%s".format(clazz, cl.size, cl.mkString("\n"))
)

/**
 * Runtime error indicating that Salat can't identify any constructor for this class.
 *  @param clazz class instance
 */
case class MissingConstructor(clazz: Class[_]) extends Error("Couldn't find a constructor for %s".format(clazz.getName))

/**
 * Runtime error indicating that Salat can't find the pickled Scala signature for this class.
 *  @param clazz class instance
 */
case class MissingPickledSig(clazz: Class[_]) extends Error("FAIL: class '%s' is missing both @ScalaSig and .class file!".format(clazz))

/**
 * Runtime error indicating that class' pickled Scala signature does not define any top-level classes or objects.
 *  @param clazz class instance
 */
case class MissingExpectedType(clazz: Class[_]) extends Error("Parsed pickled Scala signature, but no expected type found: %s"
  .format(clazz))

//case class NestingGlitch(clazz: Class[_], owner: String, outer: String, inner: String) extends Error("Didn't find owner=%s, outer=%s, inner=%s in pickled scala sig for %s"
//  .format(owner, outer, inner, clazz))

case class MissingCaseObjectOverride(path: String, value: Any, ctxName: String) extends Error(
  "Ctx='%s' does not define a case object override that can be used with class='%s' and value='%s'".
    format(ctxName, path, value)
)
