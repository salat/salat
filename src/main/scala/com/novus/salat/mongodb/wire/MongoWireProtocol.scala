package com.novus.salat.mongodb.wire

import com.novus.salat._
import org.bson._
import org.bson.io._

abstract class OpCode(val code: int32)

case object OP_REPLY        extends OpCode(1)
case object OP_MSG          extends OpCode(1000)
case object OP_UPDATE       extends OpCode(2001)
case object OP_INSERT       extends OpCode(2002)
case object RESERVED        extends OpCode(2003)
case object OP_QUERY        extends OpCode(2004)
case object OP_GET_MORE     extends OpCode(2005)
case object OP_DELETE       extends OpCode(2006)
case object OP_KILL_CURSORS extends OpCode(2007)

object `package` {
  type int32 = Int
  type int64 = Long
  type cstring = String
  type document = BSONObject

  val NotResponse = -1

  // XXX: will suffice for now, but should be a little less naive
  def requestID: int32 = (System.currentTimeMillis / 1000 - scala.util.Random.nextInt.abs).abs.intValue

  def encoder = new RichBSONEncoder

  implicit def pimpGenericFlags(flags: List[GenericFlag]) = new {
    def materialize = flags.foldLeft(BigInt(0)) {
      case (packed, flag) => flag.set(packed)
    }
  }

  implicit def bigint2int(bi: BigInt): Int = bi.toInt
}

trait GenericFlag {
  val bits: List[int32]
  def set(flags: BigInt): BigInt =
    bits.distinct.foldLeft(flags) {
      case (flags, bit) => flags.setBit(bit)
    }
}

sealed class RichBSONEncoder(val out: OutputBuffer) extends BSONEncoder {
  def this() = this(new BasicOutputBuffer)
  set(out)
}

trait Writable {
  def write(enc: BSONEncoder)
}

case class MsgHeader(requestID: int32, responseTo: int32, op: OpCode) extends Writable {
  def write(enc: BSONEncoder) = {
    enc.writeInt(requestID)
    enc.writeInt(responseTo)
    enc.writeInt(op.code)
  }
}

trait Op extends Writable {
  self =>
    val code: OpCode

  def toByteArray: Array[Byte] = {
    val enc = encoder
    self match {
      case hh: HasHeader => {
        hh.header.write(enc)
        hh match {
          case zah: ZeroAfterHeader => enc.writeInt(0)
        }
      }
      case _ =>
    }
    self.write(enc)
    enc.out.toByteArray
  }
}

trait HasHeader {
  val header: MsgHeader
}

trait ZeroAfterHeader {
  self: HasHeader =>
}

case class OpUpdate(header: MsgHeader, fullCollectionName: cstring, flags: List[OpUpdate.Flag], selector: document, update: document) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_UPDATE
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    enc.writeInt(flags.materialize)
    enc.putObject(selector)
    enc.putObject(update)
  }
}

object OpUpdate {
  abstract class Flag(val bits: List[int32], name: cstring) extends GenericFlag
  case object Update      extends Flag(List(0), "Update")
  case object MultiUpdate extends Flag(List(1), "MultiUpdate")
}

case class OpInsert(header: MsgHeader, fullCollectionName: cstring, documents: List[document]) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_INSERT
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    for (doc <- documents) enc.putObject(doc)
  }
}

case class OpQuery(header: MsgHeader, flags: List[OpQuery.Flag], fullCollectionName: cstring, numberToSkip: int32, numberToReturn: int32, query: document, returnFieldSelector: Option[document]) extends Op with HasHeader {
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
  abstract class Flag(val bits: List[int32], name: cstring) extends GenericFlag
  case object Reserved        extends Flag(List(0), "Reserved")
  case object TailableCursor  extends Flag(List(1), "TailableCursor")
  case object SlaveOk         extends Flag(List(2), "SlaveOk")
  case object OplogReplay     extends Flag(List(3), "OplogReplay")
  case object NoCursorTimeout extends Flag(List(4), "NoCursorTimeout")
  case object AwaitData       extends Flag(List(5), "AwaitData")
  case object Exhaust         extends Flag(List(6), "Exhaust")
  case object Partial         extends Flag(List(7), "Partial")

  abstract class QueryElement(name: cstring)
  case object $query    extends QueryElement("$query")
  case object $orderby  extends QueryElement("$orderby")
  case object $hint     extends QueryElement("$hint")
  case object $explain  extends QueryElement("$extends")
  case object $snapshot extends QueryElement("$snapshot")
}

case class OpGetMore(header: MsgHeader, fullCollectionName: cstring, numberToReturn: int32, cursorID: int64) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_GET_MORE
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    enc.writeInt(numberToReturn)
    enc.writeLong(cursorID)
  }
}

case class OpDelete(header: MsgHeader, fullCollectionName: cstring, flags: List[OpDelete.Flag], selector: document) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_DELETE
  def write(enc: BSONEncoder) = {
    enc.writeCString(fullCollectionName)
    enc.writeInt(flags.materialize)
    enc.putObject(selector)
  }
}

object OpDelete {
  abstract class Flag(val bits: List[int32], name: cstring) extends GenericFlag
  case object SingleRemove extends Flag(List(0), "SingleRemove")
}

case class OpKillCursors(header: MsgHeader, numberOfCursorIDs: int32, cursorIDs: List[int64]) extends Op with HasHeader with ZeroAfterHeader {
  val code = OP_KILL_CURSORS
  def write(enc: BSONEncoder) = {
    enc.writeInt(numberOfCursorIDs)
    for (cursorID <- cursorIDs) enc.writeLong(cursorID)
  }
}

case class OpReply(header: MsgHeader, flags: List[OpReply.Flag], cursorID: int64, startingFrom: int32, numberReturned: int32, documents: List[document]) extends Op with HasHeader {
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
  abstract class Flag(val bits: List[int32], name: cstring) extends GenericFlag
  case object CursorNotFound   extends Flag(List(0), "CursorNotFound")
  case object QueryFailure     extends Flag(List(1), "QueryFailure")
  case object ShardConfigStale extends Flag(List(2), "ShardConfigStale")
  case object AwaitCapable     extends Flag(List(3), "AwaitCapable")
}
