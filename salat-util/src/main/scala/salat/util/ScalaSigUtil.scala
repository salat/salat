/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-util
 * Class:         ScalaSigUtil.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package salat.util

import salat.annotations.util._

import scala.reflect.ScalaSignature
import scala.reflect.internal.pickling.ByteCodecs
import scala.tools.scalap.scalax.rules.scalasig._

protected[salat] object ScalaSigUtil extends Logging {
  def parseClassFileFromByteCode(clazz: Class[_]): Option[ClassFile] = try {
    // taken from ScalaSigParser parse method with the explicit purpose of walking away from NPE
    val byteCode = ByteCode.forClass(clazz)
    Option(ClassFileParser.parse(byteCode))
  }
  catch {
    case e: NullPointerException => None // yes, this is the exception, but it is totally unhelpful to the end user
  }

  def parseByteCodeFromAnnotation(clazz: Class[_]): Option[ByteCode] = {
    clazz.annotation[ScalaSignature] match {
      case Some(sig) if sig != null => {
        val bytes = sig.bytes.getBytes("UTF-8")
        val len = ByteCodecs.decode(bytes)
        Option(ByteCode(bytes.take(len)))
      }
      case _ => None
    }
  }

  def parseScalaSig0(_clazz: Class[_], classloaders: Iterable[ClassLoader]): Option[ScalaSig] = {

    // support case objects by selectively re-jiggering the class that has been passed in
    val clazz = if (_clazz.getName.endsWith("$")) {
      val caseObject = _clazz.getName.replaceFirst("\\$$", "")
      if (classloaders.isEmpty) {
        Class.forName(caseObject)
      }
      else {
        resolveClass_!(caseObject, classloaders)
      }
    }
    else _clazz
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

    val fromByteCode = parseClassFileFromByteCode(clazz).flatMap(ScalaSigParser.parse(_))
    if (fromByteCode.isDefined) {
      fromByteCode
    }
    else {
      val fromAnnotation = parseByteCodeFromAnnotation(clazz).map(ScalaSigAttributeParsers.parse(_))
      if (fromAnnotation.isDefined) {
        fromAnnotation
      }
      else None
    }
  }
}
