/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         SalatDAOSpec.scala
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
package com.novus.salat.test.dao

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DuplicateKeyException
import com.novus.salat._
import com.novus.salat.dao.SalatInsertError
import com.novus.salat.test._
import com.novus.salat.test.global._
import org.specs2.specification.Scope

class SalatDAOSpec extends SalatSpec {

  // which most specs can execute concurrently, this particular spec needs to execute sequentially to avoid mutating shared state,
  // namely, the MongoDB collection referenced by the AlphaDAO
  sequential
  isolated

  implicit val wc = AlphaDAO.defaultWriteConcern

  val alpha1 = Alpha(id = 1, beta = List[Beta](Gamma("gamma3"), Delta("delta3", "sampi3")))
  val alpha2 = Alpha(id = 2, beta = List[Beta](Gamma("gamma2"), Delta("delta2", "sampi2"), Delta("digamma2", "san2")))
  val alpha3 = Alpha(id = 3, beta = List[Beta](Gamma("gamma3"), Delta("delta3", "sampi3")))
  val alpha4 = Alpha(id = 4, beta = List[Beta](Delta("delta4", "koppa4")))
  val alpha5 = Alpha(id = 5, beta = List[Beta](Gamma("gamma5"), Gamma("gamma5-1")))
  val alpha6 = Alpha(id = 6, beta = List[Beta](Delta("delta6", "heta2"), Gamma("gamma6")))
  val alpha7 = Alpha(id = 7, beta = List[Beta](Delta("delta7a", "delta7b")))

  "Salat simple DAO" should {

    "supply a useful description for debugging" in {
      // default is SalatDAO[CaseClass,Id](collection name)
      AlphaDAO.description must_== "SalatDAO[Alpha,int](alpha_dao_spec)"
    }

    "insert a case class" in new alphaContext {
      val _id = AlphaDAO.insert(alpha3)
      _id must beSome(alpha3.id)
      AlphaDAO.collection.count() must_== 1L

      val dbo: MongoDBObject = MongoConnection()(SalatSpecDb)(AlphaColl).findOne().get
      grater[Alpha].asObject(dbo) must_== alpha3
    }

    "insert a collection of case classes" in new alphaContext {
      // insert returns the typed contents of _id
      val _ids = AlphaDAO.insert(alpha4, alpha5, alpha6)
      _ids must contain(Some(alpha4.id))
      _ids must contain(Some(alpha5.id))
      _ids must contain(Some(alpha6.id))
      AlphaDAO.collection.count() must_== 3L

      // the standard collection cursor returns DBOs
      val mongoCursor = AlphaDAO.collection.find()
      mongoCursor.next() must haveEntry("_id" -> alpha4.id)
      mongoCursor.next() must haveEntry("_id" -> alpha5.id)
      mongoCursor.next() must haveEntry("_id" -> alpha6.id)

      // BUT the Salat DAO returns a cursor types to case classes!
      val salatCursor = AlphaDAO.find(MongoDBObject.empty)
      salatCursor.next must_== alpha4
      salatCursor.next must_== alpha5
      salatCursor.next must_== alpha6
    }

    "no-op inserting an empty collection of objects" in {
      AlphaDAO.insert() must_== Nil
    }

    "handle MongoExceptions for duplicate key inserts" in new alphaContext {
      AlphaDAO.insert(alpha7)
      AlphaDAO.insert(alpha7) must throwA[SalatInsertError].like {
        case ex: SalatInsertError => ex.getCause must beAnInstanceOf[DuplicateKeyException]
      }
    }

    "handle DAOs using deprecated MongoConnection, throwing an exception on a WriteResult error" in new alphaContext {
      DeprecatedAlphaDAO.insert(alpha7)
      DeprecatedAlphaDAO.insert(alpha7) must throwA[SalatInsertError]
    }.pendingUntilFixed("duplicate insert fails silently when SalatDAO uses a MongoConnection instead of a MongoClient?!?")

    "support findOne returning Option[T]" in new alphaContext {
      val _ids = AlphaDAO.insert(alpha4, alpha5, alpha6)
      _ids must contain(Some(alpha4.id))
      _ids must contain(Some(alpha5.id))
      _ids must contain(Some(alpha6.id))
      AlphaDAO.collection.count() must_== 3L

      // note: you can query using an object transformed into a dbo
      AlphaDAO.findOne(grater[Alpha].asDBObject(alpha6)) must beSome(alpha6)
    }

    "support findOneById returning Option[T]" in new alphaContext {
      val _ids = AlphaDAO.insert(alpha4, alpha5, alpha6)
      _ids must contain(Some(alpha4.id))
      _ids must contain(Some(alpha5.id))
      _ids must contain(Some(alpha6.id))
      AlphaDAO.collection.count() must_== 3L

      AlphaDAO.findOneById(id = 5) must beSome(alpha5)
    }

    "support updating a case class" in new alphaContext {
      val _id = AlphaDAO.insert(alpha3)
      _id must beSome(alpha3.id)
      AlphaDAO.collection.count() must_== 1L

      // need to explicitly specify upsert and multi when updating using an object instead of dbo
      val wr = AlphaDAO.update(
        q      = MongoDBObject("_id" -> 3),
        t      = alpha3.copy(beta = List[Beta](Gamma("gamma3"))),
        upsert = false,
        multi  = false,
        wc     = new WriteConcern()
      )
      wr.getN must_== 1L

      AlphaDAO.collection.count() must_== 1L

      val dbo: MongoDBObject = MongoConnection()(SalatSpecDb)(AlphaColl).findOne().get
      grater[Alpha].asObject(dbo) must_== alpha3.copy(beta = List[Beta](Gamma("gamma3")))
    }

    "support saving a case class" in new alphaContext {
      val _id = AlphaDAO.insert(alpha3)
      _id must beSome(alpha3.id)
      AlphaDAO.collection.count() must_== 1L

      val alpha3_* = alpha3.copy(beta = List[Beta](Gamma("gamma3")))
      alpha3_* must_!= alpha3
      val wr = AlphaDAO.save(alpha3_*)
      wr.getN must_== 1L
      AlphaDAO.collection.count() must_== 1L

      val dbo: MongoDBObject = MongoConnection()(SalatSpecDb)(AlphaColl).findOne().get
      grater[Alpha].asObject(dbo) must_== alpha3_*
    }

    "support removing a case class" in new alphaContext {
      val _ids = AlphaDAO.insert(alpha4, alpha5, alpha6)
      _ids must contain(Some(alpha4.id))
      _ids must contain(Some(alpha5.id))
      _ids must contain(Some(alpha6.id))
      AlphaDAO.collection.count() must_== 3L

      val wr = AlphaDAO.remove(alpha5)
      wr.getN must_== 1L
      AlphaDAO.collection.count() must_== 2L

      AlphaDAO.findOne(grater[Alpha].asDBObject(alpha5)) must beNone

      // and then there were two!
      val salatCursor = AlphaDAO.find(MongoDBObject.empty)
      salatCursor.next must_== alpha4
      salatCursor.next must_== alpha6
    }

    "support removing by ID" in new alphaContext {
      AlphaDAO.insert(alpha1)
      AlphaDAO.collection.count() must_== 1L
      val wr = AlphaDAO.removeById(alpha1.id)
      wr.getN must_== 1L
      AlphaDAO.collection.count() must_== 0L
    }

    "support removing by a list of IDs" in new alphaContext {
      val _ids = AlphaDAO.insert(alpha4, alpha5, alpha6)
      AlphaDAO.collection.count() must_== 3L
      val wr = AlphaDAO.removeByIds(_ids.flatten)
      wr.getN must_== 3L
      AlphaDAO.collection.count() must_== 0L
    }

    "support find returning a Mongo cursor typed to a case class" in new alphaContextWithData {

      val salatCursor = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2)))
      salatCursor.next must_== alpha2
      salatCursor.next must_== alpha3
      salatCursor.next must_== alpha4
      salatCursor.next must_== alpha5
      salatCursor.next must_== alpha6
      salatCursor.hasNext must beFalse

      val salatCursor2 = AlphaDAO.find(ref = grater[Alpha].asDBObject(alpha6))
      salatCursor2.next must_== alpha6
      salatCursor2.hasNext must beFalse

      // works with limits!
      val salatCursor3 = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2))).limit(2)
      salatCursor3.next must_== alpha2
      salatCursor3.next must_== alpha3
      salatCursor3.hasNext must beFalse

      // works with limits and skip
      val salatCursor4 = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2)))
        .skip(2)
        .limit(1)
      salatCursor4.next must_== alpha4
      salatCursor4.hasNext must beFalse

      // works with limits and skip
      val salatCursor5 = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2)))
        .sort(orderBy = MongoDBObject("_id" -> -1)) // sort by _id desc
        .skip(1)
        .limit(1)
      salatCursor5.next must_== alpha5
      salatCursor5.hasNext must beFalse
    }

    "support find with a set of keys" in new alphaContextWithData {
      val salatCursor = AlphaDAO.find(
        ref  = MongoDBObject("_id" -> MongoDBObject("$lt" -> 3)),
        keys = MongoDBObject("beta" -> 0)
      ) // forces beta key to be excluded
      salatCursor.next must_== alpha1.copy(beta = Nil)
      salatCursor.next must_== alpha2.copy(beta = Nil)
      salatCursor.hasNext must beFalse
    }

    "support findOne with an object typed to the id" in new epsilonContext {
      val e = Epsilon(notes = "Just a test")
      val _id = EpsilonDAO.insert(e)
      _id must beSome(e.id)
      EpsilonDAO.collection.count() must_== 1L

      val e_* = EpsilonDAO.findOne(grater[Epsilon].asDBObject(e))
      e_* must not beNone
    }

    "support using a query to bring back a typed list of ids" in new alphaContextWithData {
      val idList = AlphaDAO.ids(MongoDBObject("_id" -> MongoDBObject("$gt" -> 2)))
      idList must haveSize(4)
      idList must contain(3, 4, 5, 6)
    }

    "support using an iterator" in new alphaContextWithData {
      val results = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2)))
        .sort(orderBy = MongoDBObject("_id" -> -1)) // sort by _id desc
        .skip(1)
        .limit(1)
        .toList // yay!
      results must haveSize(1)
      results must contain(alpha5)

    }

    "support primitive projections" in new thetaContext {
      // a projection on a findOne that matches theta1
      ThetaDAO.primitiveProjection[String](MongoDBObject("x" -> "x1"), "y") must beSome("y1")
      // a projection on a findOne that brings nothing back
      ThetaDAO.primitiveProjection[String](MongoDBObject("x" -> "x99"), "y") must beNone

      val projList = ThetaDAO.primitiveProjections[String](MongoDBObject.empty, "y")
      projList must haveSize(4)
      projList must contain("y1", "y2", "y3", "y4") // theta5 has a null value for y, not in the list
    }

    "support using a projection on an Option field to filter out Nones" in new xiContext {
      // a projection on a findOne that matches xi1
      XiDAO.primitiveProjection[String](MongoDBObject("x" -> "x1"), "y") must beSome("y1")
      // a projection on a findOne that brings nothing back
      XiDAO.primitiveProjection[String](MongoDBObject("x" -> "x99"), "y") must beNone

      val projList = XiDAO.primitiveProjections[String](MongoDBObject.empty, "y")
      projList must haveSize(4)
      projList must contain("y1", "y2", "y3", "y4") // xi5 has a null value for y, not in the list
    }

    "support case class projections" in new kappaContext {
      // a projection on a findOne that matches kappa1
      KappaDAO.projection[Nu](MongoDBObject("k" -> "k1"), "nu") must beSome(nu1)
      // a projection on a findOne that brings nothing back
      KappaDAO.projection[Nu](MongoDBObject("k" -> "k99"), "nu") must beNone

      val projList = KappaDAO.projections[Nu](MongoDBObject("k" -> MongoDBObject("$in" -> List("k2", "k3"))), "nu")
      projList must haveSize(2)
      projList must contain(nu2, nu3)
    }
  }

  trait alphaContext extends Scope {
    log.debug("before: dropping %s", AlphaDAO.collection.fullName)
    AlphaDAO.collection.drop()
    AlphaDAO.collection.isEmpty aka "alpha collection must be empty before test" must beTrue
  }

  trait alphaContextWithData extends Scope {
    log.debug("before: dropping %s", AlphaDAO.collection.fullName)
    AlphaDAO.collection.drop()
    AlphaDAO.collection.isEmpty aka "alpha collection must be empty before test" must beTrue

    val _ids = AlphaDAO.insert(alpha1, alpha2, alpha3, alpha4, alpha5, alpha6)
    _ids must contain(Option(alpha1.id), Option(alpha2.id), Option(alpha3.id), Option(alpha4.id), Option(alpha5.id), Option(alpha6.id))
    AlphaDAO.collection.count() aka "alpha collection must have 6 records before test" must_== 6L
  }

  trait epsilonContext extends Scope {
    log.debug("before: dropping %s", EpsilonDAO.collection.fullName)
    EpsilonDAO.collection.drop()
    EpsilonDAO.collection.isEmpty aka "epsilon collection must be empty before test" must beTrue
  }

  trait thetaContext extends Scope {
    log.debug("before: dropping %s", ThetaDAO.collection.fullName)
    ThetaDAO.collection.drop()
    ThetaDAO.collection.isEmpty aka "theta collection must be empty before test" must beTrue

    val theta1 = Theta(x = "x1", y = "y1")
    val theta2 = Theta(x = "x2", y = "y2")
    val theta3 = Theta(x = "x3", y = "y3")
    val theta4 = Theta(x = "x4", y = "y4")
    val theta5 = Theta(x = "x5", y = null)
    val _ids = ThetaDAO.insert(theta1, theta2, theta3, theta4, theta5)
    _ids must contain(Option(theta1.id), Option(theta2.id), Option(theta3.id), Option(theta4.id), Option(theta5.id))
    ThetaDAO.collection.count() aka "theta collection must have 5 records before test" must_== 5L
  }

  trait xiContext extends Scope {
    log.debug("before: dropping %s", XiDAO.collection.fullName)
    XiDAO.collection.drop()
    XiDAO.collection.isEmpty aka "xi collection must be empty before test" must beTrue

    val xi1 = Xi(x = "x1", y = Some("y1"))
    val xi2 = Xi(x = "x2", y = Some("y2"))
    val xi3 = Xi(x = "x3", y = Some("y3"))
    val xi4 = Xi(x = "x4", y = Some("y4"))
    val xi5 = Xi(x = "x5", y = None)
    val _ids = XiDAO.insert(xi1, xi2, xi3, xi4, xi5)
    _ids must contain(Option(xi1.id), Option(xi2.id), Option(xi3.id), Option(xi4.id), Option(xi5.id))
    XiDAO.collection.count() aka "xi collection must have 5 records before test" must_== 5L
  }

  trait kappaContext extends Scope {
    log.debug("before: dropping %s", KappaDAO.collection.fullName)
    KappaDAO.collection.drop()
    KappaDAO.collection.isEmpty aka "kappa collection must be empty before test" must beTrue

    val nu1 = Nu(x = "x1", y = "y1")
    val nu2 = Nu(x = "x2", y = "y2")
    val nu3 = Nu(x = "x3", y = "y3")

    val kappa1 = Kappa(k = "k1", nu = nu1)
    val kappa2 = Kappa(k = "k2", nu = nu2)
    val kappa3 = Kappa(k = "k3", nu = nu3)
    val _ids = KappaDAO.insert(kappa1, kappa2, kappa3)
    _ids must contain(Option(kappa1.id), Option(kappa2.id), Option(kappa3.id))
    KappaDAO.collection.count() aka "kappa collection must have 3 records before test" must_== 3L
  }
}
