/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-util
 * Class:         glitch.scala
 * Last modified: 2012-04-28 20:34:21 EDT
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

package com.novus.salat.util

import java.lang.reflect.Constructor
// a useful place to be when things go pear-shaped
// p.s. could more people throw exceptions like these?

case class TooManyConstructorsWithArgs[X <: AnyRef with Product](clazz: Class[X], cl: List[Constructor[X]]) extends Error(
  "constructor: clazz=%s ---> expected 1 constructor with args but found %d\n%s".format(clazz, cl.size, cl.mkString("\n")))

case class TooManyEmptyConstructors[X <: AnyRef with Product](clazz: Class[X], cl: List[Constructor[X]]) extends Error(
  "constructor: clazz=%s ---> expected 1 empty constructor but found %d\n%s".format(clazz, cl.size, cl.mkString("\n")))

case class MissingConstructor(clazz: Class[_]) extends Error("Couldn't find a constructor for %s".format(clazz.getName))

case class MissingPickledSig(clazz: Class[_]) extends Error("FAIL: class '%s' is missing both @ScalaSig and .class file!".format(clazz))

case class MissingExpectedType(clazz: Class[_]) extends Error("Parsed pickled Scala signature, but no expected type found: %s"
  .format(clazz))

case class MissingTopLevelClass(clazz: Class[_]) extends Error("Parsed pickled scala signature but found no top level class for: %s"
  .format(clazz))

case class NestingGlitch(clazz: Class[_], owner: String, outer: String, inner: String) extends Error("Didn't find owner=%s, outer=%s, inner=%s in pickled scala sig for %s"
  .format(owner, outer, inner, clazz))

