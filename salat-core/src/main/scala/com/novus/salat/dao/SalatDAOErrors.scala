/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         SalatDAOErrors.scala
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
package com.novus.salat.dao

import com.mongodb.casbah.TypeImports._
import com.mongodb.{DBObject, WriteConcern}

protected[dao] object SalatDAOError {
  type LegacyErrorOrMongoException = Either[WriteResult, MongoException]

  implicit class SomeKindOfMongoError(val cause: LegacyErrorOrMongoException) extends AnyVal {
    def toErrorString: String = cause.fold({ wr => s"$wr" }, { ex => s"$ex" })
  }

  private val MulitDboFailureMsg = "ONE OR MORE OF YOUR DBOs FAILED"

  def dboFailures(dbos: List[DBObject], opThatFailed: String) = {
    val single = dbos.size == 1
    s"""
       |FAILED TO ${opThatFailed.toUpperCase()} ${if (single) "DBO" else s"ONE OR MORE OF YOUR DBOs (SEE Error FOR DETAILS)"}:
       |${if (single) dbos.head else dbos.mkString("\n")}
     """.stripMargin
  }
}

import SalatDAOError._

abstract class SalatDAOError(
  whichDAO:        String,
  thingThatFailed: String,
  collection:      MongoCollection,
  wc:              WriteConcern,
  cause:           LegacyErrorOrMongoException,
  dbos:            List[DBObject]
) extends RuntimeException(s"""

    $whichDAO: $thingThatFailed failed!

    Collection: ${collection.name}
    WriteConcern: $wc
    Error: ${cause.toErrorString}

    ${dboFailures(dbos, thingThatFailed)}
 """, cause.right.toOption.orNull)

object SalatInsertError {
  @deprecated("Use MongoClient instead of MongoConnection", "1.10.0")
  def apply(
    description: String,
    collection:  MongoCollection,
    wc:          WriteConcern,
    wr:          WriteResult,
    dbos:        List[DBObject]
  ): SalatInsertError =
    SalatInsertError(description, collection, wc, Left(wr), dbos)
}

case class SalatInsertError(
  description: String,
  collection:  MongoCollection,
  wc:          WriteConcern,
  cause:       LegacyErrorOrMongoException,
  dbos:        List[DBObject]
) extends SalatDAOError(description, "insert", collection, wc, cause, dbos)

object SalatRemoveError {
  @deprecated("Use MongoClient instead of MongoConnection", "1.10.0")
  def apply(
    description: String,
    collection:  MongoCollection,
    wc:          WriteConcern,
    wr:          WriteResult,
    dbos:        List[DBObject]
  ): SalatRemoveError =
    SalatRemoveError(description, collection, wc, Left(wr), dbos)
}

case class SalatRemoveError(
  description: String,
  collection:  MongoCollection,
  wc:          WriteConcern,
  cause:       LegacyErrorOrMongoException,
  dbos:        List[DBObject]
) extends SalatDAOError(description, "remove", collection, wc, cause, dbos)

object SalatSaveError {
  @deprecated("Use MongoClient instead of MongoConnection", "1.10.0")
  def apply(
    description: String,
    collection:  MongoCollection,
    wc:          WriteConcern,
    wr:          WriteResult,
    dbos:        List[DBObject]
  ): SalatSaveError =
    SalatSaveError(description, collection, wc, Left(wr), dbos)
}

case class SalatSaveError(
  description: String,
  collection:  MongoCollection,
  wc:          WriteConcern,
  cause:       LegacyErrorOrMongoException,
  dbos:        List[DBObject]
) extends SalatDAOError(description, "save", collection, wc, cause, dbos)

abstract class SalatDAOQueryError(
  whichDAO:        String,
  thingThatFailed: String,
  collection:      MongoCollection,
  query:           DBObject,
  wc:              WriteConcern,
  cause:           LegacyErrorOrMongoException
) extends RuntimeException(s"""

    $whichDAO: $thingThatFailed failed!

    Collection: ${collection.name}
    WriteConcern: $wc
    Error: ${cause.toErrorString}

    QUERY: $query

 """, cause.right.toOption.orNull)

object SalatRemoveQueryError {
  @deprecated("Use MongoClient instead of MongoCollection", "1.10.0")
  def apply(
    whichDAO:   String,
    collection: MongoCollection,
    query:      DBObject,
    wc:         WriteConcern,
    wr:         WriteResult
  ): SalatRemoveQueryError =
    SalatRemoveQueryError(whichDAO, collection, query, wc, Left(wr))
}

case class SalatRemoveQueryError(
  whichDAO:   String,
  collection: MongoCollection,
  query:      DBObject,
  wc:         WriteConcern,
  cause:      LegacyErrorOrMongoException
) extends SalatDAOQueryError(whichDAO, "remove", collection, query, wc, cause)

object SalatDAOUpdateError {
  @deprecated("Use MongoClient instead of MongoCollection", "1.10.0")
  def apply(
    whichDAO:   String,
    collection: MongoCollection,
    query:      DBObject,
    o:          DBObject,
    wc:         WriteConcern,
    wr:         WriteResult,
    upsert:     Boolean,
    multi:      Boolean
  ): SalatDAOUpdateError =
    SalatDAOUpdateError(whichDAO, collection, query, o, wc, Left(wr), upsert, multi)
}

case class SalatDAOUpdateError(
  whichDAO:   String,
  collection: MongoCollection,
  query:      DBObject,
  o:          DBObject,
  wc:         WriteConcern,
  cause:      LegacyErrorOrMongoException,
  upsert:     Boolean,
  multi:      Boolean
) extends RuntimeException(s"""

    $whichDAO: update failed!

    Collection: ${collection.name}
    WriteConcern: $wc
    Error: ${cause.toErrorString}
    Upsert: $upsert
    Multi: $multi

    QUERY: $query

    OBJECT TO UPDATE: $o

 """, cause.right.toOption.orNull)
