/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-util
 * Class:         SalatDAOUtils.scala
 * Last modified: 2015-06-23 20:48:17 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package com.github.salat.util

/**
 * Some useful utilities for extracting a single result from a list of results.
 */
object SalatDAOUtils {

  /**
   * Return one or blow up.  You could just use the head method, but the value added here is an error message explaining
   *  how what you thought was a unique selection criteria is... not.
   *  @param list a list expected to contain a single entry
   *  @tparam Z parameterized type of list
   *  @return a single result or an error
   */
  def exactlyOne[Z](list: List[Z]): Z = list match {
    case head :: Nil => head
    case Nil         => sys.error("exactlyOne: expected exactly one but found an empty result list!")
    case list => sys.error("exactlyOne: expected exactly one result but found %d items in the result list:\n%s\n".
      format(list.size, list.mkString("\n"))) // how long is the list?  that depends on how badly things went wrong back in your collection....
  }

  /**
   * Guarantee one or none.
   *  @param list a list expected to contain a single entry or no entries
   *  @tparam Z parameterized type of list
   *  @return Some for a single entry list; None for an empty list
   */
  def oneOrNone[Z](list: List[Z]): Option[Z] = list match {
    case head :: Nil => Some(head)
    case Nil         => None
    case list => sys.error("oneOrNone: expected one or none result but found %d items in the result list:\n%s\n".
      format(list.size, list.mkString("\n")))
  }

}
