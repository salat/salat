package com.bumnetworks.salat

import java.lang.reflect.Method

import scala.math.{BigDecimal => ScalaBigDecimal}
import scala.tools.scalap.scalax.rules.scalasig._

import com.bumnetworks.salat.transformers._
import com.bumnetworks.salat.annotations.raw._
import com.bumnetworks.salat.annotations.util._

import com.mongodb.casbah.Imports._

object Field {
  def apply(idx: Int, name: String, t: TypeRefType, method: Method)(implicit ctx: Context): Field = {
    val _in = in.select(t)
    val _out = out.select(t)
    new Field(idx, method.annotation[Key].map(_.value).getOrElse(name),
              t, _in, _out, method.annotation[Ignore].map(_ => true).getOrElse(false)) {}
  }
}

sealed abstract class Field(val idx: Int, val name: String, val typeRefType: TypeRefType,
                            val in: Transformer, val out: Transformer, val ignore: Boolean)(implicit val ctx: Context) extends CasbahLogging {
  def in_!(value: Any) = in.transform_!(value)
  def out_!(value: Any) = out.transform_!(value)
}
