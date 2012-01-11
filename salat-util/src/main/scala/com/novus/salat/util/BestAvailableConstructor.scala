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
package com.novus.salat.util

import java.lang.reflect.{ Modifier, Constructor }

object BestAvailableConstructor extends Logging {
  def apply[X <: AnyRef with Product](clazz: Class[X]): Constructor[X] = {
    val cl = clazz.getConstructors.toList.asInstanceOf[List[Constructor[X]]]
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