package com.novus.salat.util

import com.novus.salat.annotations.util._
import com.novus.salat.util.Logging
import reflect.ScalaSignature
import reflect.generic.ByteCodecs
import scala.tools.scalap.scalax.rules.scalasig._

object ScalaSigUtil extends Logging {
  private[salat] def parseClassFileFromByteCode(clazz: Class[_]): Option[ClassFile] = try {
    // taken from ScalaSigParser parse method with the explicit purpose of walking away from NPE
    val byteCode  = ByteCode.forClass(clazz)
    Option(ClassFileParser.parse(byteCode))
  }
  catch {
    case e: NullPointerException => None  // yes, this is the exception, but it is totally unhelpful to the end user
  }

  private[salat] def parseByteCodeFromAnnotation(clazz: Class[_]): Option[ByteCode] = {
    clazz.annotation[ScalaSignature] match {
      case Some(sig) if sig != null => {
        val bytes = sig.bytes.getBytes("UTF-8")
        val len = ByteCodecs.decode(bytes)
        Option(ByteCode(bytes.take(len)))
      }
      case _ => None
    }
  }

  private[salat] def parseScalaSig0(_clazz: Class[_]): Option[ScalaSig] = {

    // support case objects by selectively re-jiggering the class that has been passed in
    val clazz = if (_clazz.getName.endsWith("$")) Class.forName(_clazz.getName.replaceFirst("\\$$", "")) else _clazz
    assume(clazz != null, "parseScalaSig0: cannot parse ScalaSig from null class=%s".format(_clazz))

//    val sigFromAnnotation = parseByteCodeFromAnnotation(clazz).map(ScalaSigAttributeParsers.parse(_))
//    val sigFromBytes: Option[ScalaSig] = parseClassFileFromByteCode(clazz).map(ScalaSigParser.parse(_)).getOrElse(None)

//    log.info("""
//
//     parseScalaSig0: clazz=%s --->
//     FROM ANNOTATION? %s
//     FROM BYTES? %s
//
//     """, clazz, sigFromAnnotation.isDefined, sigFromBytes.isDefined)

    parseClassFileFromByteCode(clazz).map(ScalaSigParser.parse(_)).getOrElse(None) orElse
    parseByteCodeFromAnnotation(clazz).map(ScalaSigAttributeParsers.parse(_)) orElse
    None
  }
}