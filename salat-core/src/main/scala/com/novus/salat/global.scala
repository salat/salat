/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         global.scala
 * Last modified: 2012-06-27 23:42:09 EDT
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
package com.novus.salat

package object global {

  implicit val ctx = new Context {
    val name = "global"
  }

  // example of a context that uses type hints when necessary
  @deprecated("Please create your own custom context", "1.9-SNAPSHOT") val WhenNecessary = new Context {
    val name = "global-when-necessary"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = TypeHint)
  }

  // example of a context that never uses type hints
  @deprecated("Please create your own custom context", "1.9-SNAPSHOT") val NoTypeHints = new Context {
    val name = "global-no-type-hints"
    override val typeHintStrategy = NeverTypeHint
  }
}
