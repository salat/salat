/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         CustomGrater.scala
 * Last modified: 2016-07-10 23:53:02 EDT
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

package salat

import com.mongodb.DBObject
import com.mongodb.casbah.commons.Implicits._
import com.mongodb.casbah.commons.MongoDBObject
import salat.json.ToJField
import salat.transformers.CustomTransformer
import org.json4s.JsonAST.JObject
import org.json4s._

class CustomGrater[ModelObject <: AnyRef](
    clazz:       Class[ModelObject],
    transformer: CustomTransformer[ModelObject, DBObject]
)(implicit ctx: Context) extends Grater[ModelObject](clazz)(ctx) {

  def asDBObject(o: ModelObject) = transformer.serialize(o)

  def asObject[A <% MongoDBObject](dbo: A) = transformer.deserialize(unwrapDBObj(dbo))

  def toMap(o: ModelObject) = transformer.serialize(o).toMap.asInstanceOf[Map[String, Any]]

  def fromMap(m: Map[String, Any]) = transformer.deserialize(m.asDBObject)

  def toJSON(o: ModelObject) = {
    val builder = List.newBuilder[JField]
    builder ++= ToJField.typeHint(clazz, ctx.typeHintStrategy.when == TypeHintFrequency.Always)
    transformer.serialize(o).foreach {
      case (k, v) => builder += ToJField(k, v)
    }
    JObject(builder.result())
  }

  def fromJSON(j: JObject) = transformer.deserialize(j.values.asDBObject)

  def iterateOut[T](o: ModelObject, outputNulls: Boolean)(f: ((String, Any)) => T) = Iterator.empty
}
