package com.novus.salat.util

/**
 * Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/novus/salat
 *
 */

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

