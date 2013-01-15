/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         TypeParamsModel.scala
 * Last modified: 2012-10-15 20:40:58 EDT
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */

package com.novus.salat.test.model

//  @author akraievoy@gmail.com

case class CC(val field: CharSequence)

case class CCwithTypePar[T](val typeParamField: CCwithTypeParNest[T])

case class CCwithTypeParNest[T](val typeParamField: T)

case class CCwithCovarTP[+T](val typeParamField: CCwithCovarTPNest[T])

case class CCwithCovarTPNest[+T](val typeParamField: T)

case class CCwithCTPAndExistentialField[+T](val typeParamField: CCwithCTPAndExistentialFieldNest[_ <: T])

case class CCwithCTPAndExistentialFieldNest[+T](val typeParamField: T)
