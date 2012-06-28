/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-util
 * Class:         AsyncSalat.scala
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

/** Helper class for running Salat with Android.
 *  @param f function returning Any
 *  @param r function that takes an Either outcome as input and does something by side effect
 */
class AsyncSalatRunnable(f: => Any)(r: Either[Throwable, Any] => Unit) extends Runnable {
  def run {
    try {
      r(Right(f))
    }
    catch {
      case t => r(Left(t))
    }
  }
}

