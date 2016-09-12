/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-util
 * Class:         BestAvailableConstructorSpec.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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

package salat.util

import salat.util.model._
import org.specs2.mutable.Specification

class BestAvailableConstructorSpec extends Specification with Logging {

  "BestAvailableConstructor" should {
    "find a constructor with args when one is present" in {
      val clazz = classOf[OneConstructorWithArgs]
      BestAvailableConstructor(clazz) must_== clazz.getConstructors.head
    }
    "discard an empty constructor in favour of one with args" in {
      val clazz = classOf[OneConstructorWithArgsOneEmpty]
      BestAvailableConstructor(clazz) must_== clazz.getConstructors.filter(_.getParameterTypes.nonEmpty).head
    }
    "throw an exception if more than one constructor with args is present" in {
      val clazz = classOf[TwoConstructorsWithArgs]
      BestAvailableConstructor(clazz) must throwA[TooManyConstructorsWithArgs[TwoConstructorsWithArgs]]
    }
    "allow an empty constructor if no better alternative exists" in {
      val clazz = classOf[OnlyEmptyConstructor]
      BestAvailableConstructor(clazz) must_== clazz.getConstructors.head
    }
  }

}
