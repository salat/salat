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
package com.novus.salat.util

object SalatDAOUtils {

  /**
    * Return one or blow up.  You could just use the head method, but the value added here is an error message explaining
    * how what you thought was a unique selection criteria is... not.  Using head would mask this error, assuming you care.
   */
  def exactlyOne[Z](list: List[Z]): Z = list match {
    case head :: Nil => head
    case Nil => throw new Error("exactlyOne: expected exactly one but found an empty result list!")
    case list => throw new Error("exactlyOne: expected exactly one result but found %d items in the result list:\n%s\n".
      format(list.size, list.mkString("\n"))) // how long is the list?  that depends on how badly things went wrong back in your collection....
  }

  /**
    * Guarantee one or none.
    */
  def oneOrNone[Z](list: List[Z]): Option[Z] = list match {
    case head :: Nil => Some(head)
    case Nil => None
    case list => throw new Error("oneOrNone: expected one or none result but found %d items in the result list:\n%s\n".
      format(list.size, list.mkString("\n")))
  }

}