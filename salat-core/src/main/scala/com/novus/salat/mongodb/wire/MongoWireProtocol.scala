package com.novus.salat.mongodb.wire

import java.io.BufferedInputStream

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._

import org.bson._
import org.bson.io._

import com.novus.salat.util.Logging

abstract class OpCode(val code: Int)

case object OP_REPLY extends OpCode(1)
case object OP_MSG extends OpCode(1000)
case object OP_UPDATE extends OpCode(2001)
case object OP_INSERT extends OpCode(2002)
case object RESERVED extends OpCode(2003)
case object OP_QUERY extends OpCode(2004)
case object OP_GET_MORE extends OpCode(2005)
case object OP_DELETE extends OpCode(2006)
case object OP_KILL_CURSORS extends OpCode(2007)

object OpCode {
  val * = {
    OP_REPLY :: OP_MSG :: OP_UPDATE :: OP_INSERT :: RESERVED ::
      OP_QUERY :: OP_GET_MORE :: OP_DELETE :: OP_KILL_CURSORS :: Nil
  }

  def apply(code: Int): OpCode = * find { _.code == code } getOrElse { throw new RuntimeException("bad op code: %d".format(code)) }
}

object `package` {
  // XXX: will suffice for now, but should be a little less naive
  def requestID: Int = (System.currentTimeMillis / 1000 - scala.util.Random.nextInt.abs).abs.intValue

  def encoder = new RichBSONEncoder

  implicit def pimpGenericFlags(flags: List[GenericFlag]) = new {
    def materialize = flags.foldLeft(BigInt(0)) {
      case (packed, flag) => flag.set(packed)
    }
  }

  implicit def bigint2int(bi: BigInt): Int = bi.toInt
}

trait GenericFlag {
  val bits: List[Int]
  def set(flags: BigInt): BigInt =
    bits.distinct.foldLeft(flags) {
      case (flags, bit) => flags.setBit(bit)
    }
}

sealed class RichBSONEncoder(val out: OutputBuffer) extends BSONEncoder with Logging {
  def this() = this(new BasicOutputBuffer)
  set(out)

  def grateObject(o: CaseClass): Unit = {
    val g = implicitly[Context].lookup_!(o.getClass.getName).asInstanceOf[Grater[CaseClass]]
    // What follows is what can pass for a straight-up port of
    // BSONEncoder#putObject(String, BSONObject). Except there's no
    // intermediary BSONObject.

    g.iterateOut(o) {
      case (key, value) => {
        log.info("grateObject(%s) => %s", o.getClass.getName, key)
      }
    }
  }
}

trait Writable {
  def write(enc: BSONEncoder)
}

trait Readable[T] {
  def read(in: BufferedInputStream): Option[(Int, T)]
}

case class MsgHeader(requestID: Int, responseTo: Option[Int], op: OpCode) extends Writable {
  def write(enc: BSONEncoder) = {
    enc.writeInt(requestID)

    // XXX: what should this be if message is a request?
    enc.writeInt(responseTo.getOrElse(-1))

    enc.writeInt(op.code)
  }
}

object MsgHeader extends Readable[MsgHeader] with Logging {
  def read(in: BufferedInputStream): Option[(Int, MsgHeader)] = {
    val len = Bits.readInt(in) - (32 / 8) * 4
    Some(len -> MsgHeader(requestID = Bits.readInt(in),
      responseTo = Some(Bits.readInt(in)),
      op = OpCode(Bits.readInt(in))))
  }
}

@Salat
trait Op extends Writable with Logging {
  self =>
  val code: OpCode

  def toByteArray: Array[Byte] = {
    val bytes = {
      val tmp = encoder
      self match {
        case hh: HasHeader => {
          hh.header.write(tmp)
          hh match {
            case zah: ZeroAfterHeader => tmp.writeInt(0)
            case _                    =>
          }
        }
        case _ =>
      }
      self.write(tmp)
      tmp.out.toByteArray
    }
    val enc = encoder
    enc.writeInt(32 / 8 + bytes.size)
    enc.out.write(bytes)
    log.trace("WRITE: %s", implicitly[Context].lookup_!(self.getClass.getName).asInstanceOf[Grater[CaseClass]].asDBObject(self.asInstanceOf[CaseClass]))
    enc.out.toByteArray
  }
}

trait HasHeader {
  val header: MsgHeader
}

trait ZeroAfterHeader {
  self: HasHeader =>
}

case class OpUpdate(header: MsgHeader, fullCollectionName: String, flags: List[OpUpdate.Flag], selector: BSONObject, update: BSONObject) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_UPDATE
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    enc.writeInt(flags.materialize)
    enc.putObject(selector)
    enc.putObject(update)
  }
}

object OpUpdate {
  abstract class Flag(val bits: List[Int], name: String) extends GenericFlag
  case object Update extends Flag(List(0), "Update")
  case object MultiUpdate extends Flag(List(1), "MultiUpdate")
}

case class OpInsert(header: MsgHeader, fullCollectionName: String, documents: List[BSONObject]) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_INSERT
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    for (doc <- documents) enc.putObject(doc)
  }
}

case class OpQuery(header: MsgHeader, flags: List[OpQuery.Flag], fullCollectionName: String, numberToSkip: Int, numberToReturn: Int, query: BSONObject, returnFieldSelector: Option[BSONObject]) extends Op with HasHeader {
  val code = OP_QUERY
  def write(enc: BSONEncoder) = {
    enc.writeInt(flags.materialize)
    enc.writeCString(fullCollectionName)
    enc.writeInt(numberToSkip)
    enc.writeInt(numberToReturn)
    enc.putObject(query)
    for (fields <- returnFieldSelector) enc.putObject(fields)
  }
}

object OpQuery {
  abstract class Flag(val bits: List[Int], name: String) extends GenericFlag
  case object Reserved extends Flag(List(0), "Reserved")
  case object TailableCursor extends Flag(List(1), "TailableCursor")
  case object SlaveOk extends Flag(List(2), "SlaveOk")
  case object OplogReplay extends Flag(List(3), "OplogReplay")
  case object NoCursorTimeout extends Flag(List(4), "NoCursorTimeout")
  case object AwaitData extends Flag(List(5), "AwaitData")
  case object Exhaust extends Flag(List(6), "Exhaust")
  case object Partial extends Flag(List(7), "Partial")

  abstract class QueryElement(name: String)
  case object $query extends QueryElement("$query")
  case object $orderby extends QueryElement("$orderby")
  case object $hint extends QueryElement("$hint")
  case object $explain extends QueryElement("$extends")
  case object $snapshot extends QueryElement("$snapshot")
}

case class OpGetMore(header: MsgHeader, fullCollectionName: String, numberToReturn: Int, cursorID: Long) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_GET_MORE
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    enc.writeInt(numberToReturn)
    enc.writeLong(cursorID)
  }
}

case class OpDelete(header: MsgHeader, fullCollectionName: String, flags: List[OpDelete.Flag], selector: BSONObject) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_DELETE
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    enc.writeInt(flags.materialize)
    enc.putObject(selector)
  }
}

object OpDelete {
  abstract class Flag(val bits: List[Int], name: String) extends GenericFlag
  case object SingleRemove extends Flag(List(0), "SingleRemove")
}

case class OpKillCursors(header: MsgHeader, numberOfCursorIDs: Int, cursorIDs: List[Long]) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_KILL_CURSORS
  def write(enc: BSONEncoder) = {
    enc.writeInt(numberOfCursorIDs)
    for (cursorID <- cursorIDs) enc.writeLong(cursorID)
  }
}

case class OpReply(header: MsgHeader, flags: List[OpReply.Flag], cursorID: Long, startingFrom: Int, numberReturned: Int, documents: List[BSONObject]) extends Op with HasHeader {
  val code = OP_REPLY
  def write(enc: BSONEncoder) = {
    enc.writeInt(flags.materialize)
    enc.writeLong(cursorID)
    enc.writeInt(startingFrom)
    enc.writeInt(numberReturned)
    for (doc <- documents) enc.putObject(doc)
  }
}

object OpReply {
  abstract class Flag(val bits: List[Int], name: String) extends GenericFlag
  case object CursorNotFound extends Flag(List(0), "CursorNotFound")
  case object QueryFailure extends Flag(List(1), "QueryFailure")
  case object ShardConfigStale extends Flag(List(2), "ShardConfigStale")
  case object AwaitCapable extends Flag(List(3), "AwaitCapable")
}

case class OpMsg(header: MsgHeader, message: String) extends Op with HasHeader {
  val code = OP_MSG
  def write(enc: BSONEncoder) = enc.writeCString(message)
}
