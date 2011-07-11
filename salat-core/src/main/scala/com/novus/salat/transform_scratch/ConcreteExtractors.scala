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

object StraightThroughOptionExtractor extends OptionExtractor with StraightThrough
object OptionBigDecimalExtractor extends OptionExtractor with SBigDecimalToDouble
object OptionBigIntExtractor extends OptionExtractor with BigIntToLong
object OptionCharExtractor extends OptionExtractor with CharToString
object OptionFloatExtractor extends OptionExtractor with FloatToDouble
object OptionEnumExtractor extends OptionExtractor with EnumDeflator
object OptionCaseClassExtractor extends OptionExtractor with InContextExtractor

object StraightThroughTraversableExtractor extends StraightThrough with TraversableExtractor
object TraversableBigDecimalExtractor extends SBigDecimalToDouble with TraversableExtractor
object TraversableBigIntExtractor extends BigIntToLong with TraversableExtractor
object TraversableCharExtractor extends CharToString with TraversableExtractor
object TraversableFloatExtractor extends FloatToDouble with TraversableExtractor
object TraversableEnumExtractor extends EnumDeflator with TraversableExtractor
object TraversableCaseClassExtractor extends InContextExtractor with TraversableExtractor

object StraightThroughMapExtractor extends StraightThrough with MapExtractor
object MapBigDecimalExtractor extends SBigDecimalToDouble with MapExtractor
object MapBigIntExtractor extends BigIntToLong with MapExtractor
object MapCharExtractor extends CharToString with MapExtractor
object MapFloatExtractor extends FloatToDouble with MapExtractor
object MapEnumExtractor extends EnumDeflator with MapExtractor
object MapCaseClassExtractor extends InContextExtractor with MapExtractor

object StraightThroughExtractor extends StraightThrough
object BigDecimalExtractor extends SBigDecimalToDouble
object BigIntExtractor extends BigIntToLong
object CharExtractor extends CharToString
object FloatExtractor extends FloatToDouble
object EnumExtractor extends EnumDeflator
object CaseClassExtractor extends InContextExtractor