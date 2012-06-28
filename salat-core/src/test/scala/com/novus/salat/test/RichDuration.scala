/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         RichDuration.scala
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

package com.novus.salat.test

import org.joda.time.{ DateTimeZone, Period, DateTime }
import org.joda.time.format.{ PeriodFormatterBuilder, PeriodFormat }
import scala.collection.mutable.ArrayBuffer

object RichDuration {

  val TreatZeroAsNull = 0L

  implicit def l2Rd(in: Long) = new RichDuration(in)

  implicit def l2Rd(in: Option[Long]) = new RichDuration(in.filter(_ != TreatZeroAsNull).getOrElse(TreatZeroAsNull))

  val TersePeriodFormat = new PeriodFormatterBuilder()
    .appendYears().appendSuffix("y ")
    .appendMonths().appendSuffix("mo ")
    .appendDays().appendSuffix("d ")
    .appendHours().appendSuffix("h ")
    .appendMinutes().appendSuffix("m ")
    .appendSeconds().appendSuffix("s ")
    .appendMillis().appendSuffix("ms")
    .printZeroNever()
    .toFormatter

  def avg(arr: ArrayBuffer[Long]): Double = {
    if (arr.nonEmpty) arr.foldLeft(0d)(_ + _) / arr.size else 0d
  }

  def median(arr: ArrayBuffer[Long]): Double = if (arr.nonEmpty) {
    val sorted = arr.sorted
    if (sorted.size % 2 == 1) {
      sorted((sorted.size + 1) / 2 - 1)
    }
    else {
      val lower = sorted(sorted.size / 2 - 1)
      val upper = sorted(sorted.size / 2)
      (lower + upper) / 2d
    }
  }
  else 0d
}

class RichDuration(in: Long) {

  import RichDuration._

  lazy val asDate = if (in != TreatZeroAsNull) new DateTime(in) else null

  lazy val asOption = if (in != TreatZeroAsNull) Some(new DateTime(in)) else None

  lazy val asPeriod = if (in != TreatZeroAsNull) new Period(in) else null

  def prettyPrint = if (asPeriod != null) PeriodFormat.getDefault.print(asPeriod) else ""

  def tersePrint = if (asPeriod != null) TersePeriodFormat.print(asPeriod) else ""

  def asUTC: Option[DateTime] = if (in != TreatZeroAsNull) Some(asDate.withZone(DateTimeZone.UTC)) else None

  def diff(l: Long) = if (in != TreatZeroAsNull && l != TreatZeroAsNull) Some(in - l) else None

  // type erasure sucks
  def diff[A](o: Option[A]) = if (in != TreatZeroAsNull) {
    o match {
      case Some(l: Long) if l != TreatZeroAsNull => Some(in - l)
      case Some(d: DateTime)                     => Some(in - d.getMillis)
      case x                                     => None
    }
  }
  else None

  def diff(d: DateTime) = if (in != TreatZeroAsNull) Some(in - d.getMillis) else None

  def prettyPrintDiff(l: Long) = diff(l).map(ms => PeriodFormat.getDefault.print(new Period(ms))).getOrElse("")

  def prettyPrintDiff(d: DateTime) = diff(d).map(ms => PeriodFormat.getDefault.print(new Period(ms))).getOrElse("")

  def prettyPrintDiff[A](o: Option[A]) = diff(o).map(ms => PeriodFormat.getDefault.print(new Period(ms))).getOrElse("")

  def terseDiff(d: DateTime) = diff(d).map(ms => TersePeriodFormat.print(new Period(ms))).getOrElse("")

  def terseDiff(l: Long) = diff(l).map(ms => TersePeriodFormat.print(new Period(ms))).getOrElse("")

  def terseDiff[A](o: Option[A]) = diff(o).map(ms => TersePeriodFormat.print(new Period(ms))).getOrElse("")
}
