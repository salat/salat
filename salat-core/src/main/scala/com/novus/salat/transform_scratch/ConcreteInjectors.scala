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
package com.novus.salat.transform_scratch

object StraightThroughOptionInjector extends OptionInjector with StraightThrough
object OptionBigDecimalInjector extends OptionInjector with DoubleToSBigDecimal
object OptionIntInjector extends OptionInjector with LongToInt
object OptionBigIntInjector extends OptionInjector with LongToBigInt
object OptionCharInjector extends OptionInjector with StringToChar
object OptionFloatInjector extends OptionInjector with DoubleToFloat
object OptionDateTimeInjector extends OptionInjector with DateToJodaDateTime
object OptionEnumInjector extends OptionInjector with EnumInflation
object OptionCaseClassInjector extends OptionInjector with InContextInjector


object StraightThroughTraversableInjector extends StraightThrough with TraversableInjector
object TraversableBigDecimalInjector extends DoubleToSBigDecimal with TraversableInjector
object TraversableIntInjector extends LongToInt with TraversableInjector
object TraversableBigIntInjector extends LongToBigInt with TraversableInjector
object TraversableCharInjector extends StringToChar with TraversableInjector
object TraversableFloatInjector extends DoubleToFloat with TraversableInjector
object TraversableDateTimeInjector extends DateToJodaDateTime with TraversableInjector
object TraversableEnumInjector extends EnumInflation with TraversableInjector
object TraversableCaseClassInjector extends InContextInjector with TraversableInjector

object StraightThroughMapInjector extends StraightThrough with MapInjector
object MapBigDecimalInjector extends DoubleToSBigDecimal with MapInjector
object MapIntInjector extends LongToInt with MapInjector
object MapBigIntInjector extends LongToBigInt with MapInjector
object MapCharInjector extends StringToChar with MapInjector
object MapFloatInjector extends DoubleToFloat with MapInjector
object MapDateTimeInjector extends DateToJodaDateTime with MapInjector
object MapEnumInjector extends EnumInflation with MapInjector
object MapCaseClassInjector extends InContextInjector with MapInjector

object StraightThroughInjector extends StraightThrough
object BigDecimalInjector extends DoubleToSBigDecimal
object IntInjector extends LongToInt
object BigIntInjector extends LongToBigInt
object CharInjector extends StringToChar
object FloatInjector extends DoubleToFloat
object DateTimeInjector extends DateToJodaDateTime
object EnumInjector extends EnumInflation
object CaseClassInjector extends InContextInjector
