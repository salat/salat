/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-util
 * Class:         BestAvailableConstructor.scala
 * Last modified: 2012-06-28 15:37:34 EDT
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

/** Given multiple contructors, attempt to determine the best available constructor for instantiating the class.
 */
object BestAvailableConstructor extends Logging {

  /** @param clazz parameterized class instance
   *  @tparam A any ref
   *  @return parameterized constructor instance
   */
  def apply[A](clazz: Class[A]): Constructor[A] = {
    val cl = clazz.getConstructors.toList.asInstanceOf[List[Constructor[A]]]
    //    log.info("constructor: found %d:\n%s", cl.size, cl.mkString("\n"))
    if (cl.isEmpty) {
      throw MissingConstructor(clazz)
    }
    else {
      val (someArgs, noArgs) = cl.partition(_.getParameterTypes.nonEmpty)
      //      log.info("constructor: someArgs ---> found %d:\n%s", someArgs.size, someArgs.mkString("\n"))
      //      log.info("constructor: noArgs ---> found %d:\n%s", noArgs.size, noArgs.mkString("\n"))
      someArgs match {
        case head :: Nil  => head
        case head :: tail => throw TooManyConstructorsWithArgs(clazz, someArgs)
        case Nil => noArgs match {
          case head :: _ => {
            log.warning("constructor: clazz='%s' ---> found only empty constructor '%s'", clazz, head)
            head
          }
          case Nil => throw new MissingConstructor(clazz)
        }
      }
    }
  }
}