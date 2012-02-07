package com.novus.salat

import com.novus.salat.util.Logging
import java.math.{ BigInteger, MathContext }

sealed trait BigDecimalStrategy extends Logging {

  val mathCtx: MathContext

  def out(x: BigDecimal): Any

  /** To provide backward compatibility with different serialization output, be as forgiving as possible when deserializing
   *  @param x anything that could reasonably be coerced into BigDecimal-hood
   *  @return BigDecimal
   */
  def in(x: Any) = {
    // log.info("in: clazz='%s'", x.asInstanceOf[AnyRef].getClass.toString)
    x match {
      case x: java.math.BigDecimal  => x
      case x: scala.math.BigDecimal => x // it doesn't seem as if this could happen, BUT IT DOES.  ugh.
      case d: java.lang.Double      => BigDecimal(d.toString, mathCtx)
      case d: Double                => BigDecimal(d.toString, mathCtx)
      case l: Long                  => BigDecimal(l.toString, mathCtx) // sometimes BSON handles a whole number big decimal as a Long...
      case i: Int                   => BigDecimal(i.toString, mathCtx)
      case f: Float                 => BigDecimal(f.toString, mathCtx)
      case s: Short                 => BigDecimal(s.toString, mathCtx)
      case s: String                => BigDecimal(s, mathCtx)
      case c: Array[Char]           => BigDecimal(c, mathCtx)
      case b: Array[Byte] => {
        var idx = 0
        val iter = b.iterator
        val arr = new Array[Char](b.size)
        while (iter.hasNext) {
          arr.update(idx, (iter.next() & 0xff).toChar)
          idx += 1
        }
        BigDecimal(arr, mathCtx)
      }
    }
  }
}

case class BigDecimalToStringStrategy(mathCtx: MathContext = DefaultMathContext) extends BigDecimalStrategy {
  def out(x: BigDecimal) = x.toString()
}

case class BigDecimalToDoubleStrategy(mathCtx: MathContext = DefaultMathContext) extends BigDecimalStrategy {
  def out(x: BigDecimal) = x.doubleValue()
}

case class BigDecimalToBinaryStrategy(mathCtx: MathContext = DefaultMathContext) extends BigDecimalStrategy {
  def out(x: BigDecimal) = {
    // only supports US-ASCII, since nothing else would be produced by layoutChars impl in BigDecimal
    // faster than x.toString().getBytes(Charset.defaultCharset()) -- also more efficient as default charset might repreent chars as two bytes
    val carr = x.toString().toCharArray // TODO: ??? which is more efficient, charAt on a String or toCharArray
    val arr = new Array[Byte](carr.size)
    val iter = carr.iterator
    var idx = 0
    while (iter.hasNext) {
      arr.update(idx, iter.next().asInstanceOf[Byte])
      idx += 1
    }
    arr
  }
}

sealed trait BigIntStrategy {
  def out(bi: BigInt): Any
  def out(bi: java.math.BigInteger): Any
  def out(l: Long): Any
  def out(i: Int): Any

  def in(x: Any) = x match {
    case s: String                => BigInt(x = s, radix = 10)
    case ba: Array[Byte]          => BigInt(ba)
    case bi: BigInt               => bi
    case bi: java.math.BigInteger => bi
    case l: Long                  => BigInt(l)
    case i: Int                   => BigInt(i)
  }
}

case object BigIntToBinaryStrategy extends BigIntStrategy {
  def out(bi: BigInt) = bi.toByteArray

  def out(bi: BigInteger) = bi.toByteArray

  def out(i: Int) = BigInt(i).toByteArray

  def out(l: Long) = BigInt(l).toByteArray
}

case object BigIntToLongStrategy extends BigIntStrategy {
  def out(bi: BigInt) = bi.longValue()

  def out(bi: BigInteger) = bi.longValue()

  def out(i: Int) = i.longValue()

  def out(l: Long) = l
}
