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
 */
package com.novus.salat.util

import com.novus.salat.annotations.util._
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