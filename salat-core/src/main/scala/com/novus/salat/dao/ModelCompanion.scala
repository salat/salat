package com.novus.salat.dao

import com.mongodb.casbah.Imports._
import com.novus.salat._
import net.liftweb.json.JsonAST.JObject
import java.lang.reflect.{ Type, ParameterizedType }
import com.novus.salat.util.Logging

/** Play framework style model companion
 *  @tparam ObjectType type of object to be serialized
 *  @tparam ID type of object id to be serialized
 */
trait ModelCompanion[ObjectType <: AnyRef, ID <: Any] extends Logging {

  def dao: DAO[ObjectType, ID]

  //
  // convenient access to methods on Grater
  //

  def toDBObject(t: ObjectType): DBObject = dao._grater.asDBObject(t)

  // TODO: bring back view bound...  assuming it could possibly be worth the bother.
  def toObject(dbo: DBObject): ObjectType = dao._grater.asObject(dbo)

  def toJson(t: ObjectType): JObject = dao._grater.toJSON(t)

  def toPrettyJson(t: ObjectType): String = dao._grater.toPrettyJSON(t)

  def toCompactJson(t: ObjectType): String = dao._grater.toCompactJSON(t)

  //
  // convenient access to methods on SalatDAO
  //

  def count(q: DBObject, fieldsThatMustExist: List[String], fieldsThatMustNotExist: List[String]) = dao.count(q, fieldsThatMustExist, fieldsThatMustExist)

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B) = dao.find(ref, keys)

  def find[A <% DBObject](ref: A) = dao.find(ref)

  def findOne[A <% DBObject](t: A) = dao.findOne(t)

  def findOneByID(id: ID) = dao.findOneByID(id)

  def ids[A <% DBObject](query: A) = dao.ids(query)

  def insert(docs: ObjectType*)(implicit wc: WriteConcern) = dao.insert(docs: _*)(wc)

  def insert(t: ObjectType) = dao.insert(t)

  def insert(t: ObjectType, wc: WriteConcern = dao.collection.writeConcern) = dao.insert(t, wc)

  def primitiveProjection[P](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.primitiveProjection[P](query, field)

  def primitiveProjections[P](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.primitiveProjections[P](query, field)

  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.projection[P](query, field)

  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.projections[P](query, field)

  def remove(t: ObjectType) {
    dao.remove(t)
  }

  def remove(t: ObjectType, wc: WriteConcern = dao.collection.writeConcern) {
    dao.remove(t, wc)
  }

  def remove[A <% DBObject](q: A) {
    dao.remove(q)
  }

  def remove[A <% DBObject](q: A, wc: WriteConcern) {
    dao.remove(q, wc)
  }

  def removeById(id: ID, wc: WriteConcern = dao.collection.writeConcern) {
    dao.removeById(id, wc)
  }

  def removeByIds(ids: List[ID], wc: WriteConcern = dao.collection.writeConcern) {
    dao.removeByIds(ids, wc)
  }

  def save(t: ObjectType) {
    dao.save(t)
  }

  def save(t: ObjectType, wc: WriteConcern = dao.collection.writeConcern) {
    dao.save(t, wc)
  }

  def update[A <% DBObject, B <% DBObject](q: A, o: B, upsert: Boolean, multi: Boolean, wc: WriteConcern) {
    dao.update(q, o, upsert, multi, wc)
  }

  def update[A <% DBObject](q: A, o: ObjectType, upsert: Boolean = false, multi: Boolean = false, wc: WriteConcern = dao.collection.writeConcern) {
    dao.update(q, o, upsert, multi, wc)
  }

  //
  // methods I can't see the point of personally, but which pay some distant obeisance to the Platonic DAO carried
  // forward like pyramid blocks by my predecessors
  //

  def findAll(): Iterator[ObjectType] = dao.find(MongoDBObject.empty).toIterator
}
