/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         SalatDAOErrors.scala
 * Last modified: 2015-06-23 20:48:17 EDT
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
package com.github.salat.dao

import com.mongodb.casbah.TypeImports._
import com.mongodb.{DBObject, WriteConcern}

abstract class SalatDAOError(
  whichDAO:        String,
  thingThatFailed: String,
  collection:      MongoCollection,
  wc:              WriteConcern,
  wr:              WriteResult,
  dbos:            List[DBObject]
) extends Error("""

    %s: %s failed!

    Collection: %s
    WriteConcern: %s
    WriteResult: %s

    FAILED TO %s %s
    %s


 """.format(whichDAO, thingThatFailed,
  collection.name, wc, wr,
  thingThatFailed.toUpperCase,
  if (dbos.size == 1) "DBO" else "DBOs",
  if (dbos.size == 1) dbos.head else dbos.mkString("\n")))

case class SalatInsertError(
  description: String,
  collection:  MongoCollection,
  wc:          WriteConcern,
  wr:          WriteResult,
  dbos:        List[DBObject]
) extends SalatDAOError(description, "insert", collection, wc, wr, dbos)

case class SalatRemoveError(
  description: String,
  collection:  MongoCollection,
  wc:          WriteConcern,
  wr:          WriteResult,
  dbos:        List[DBObject]
) extends SalatDAOError(description, "remove", collection, wc, wr, dbos)

case class SalatSaveError(
  description: String,
  collection:  MongoCollection,
  wc:          WriteConcern,
  wr:          WriteResult,
  dbos:        List[DBObject]
) extends SalatDAOError(description, "save", collection, wc, wr, dbos)

abstract class SalatDAOQueryError(
  whichDAO:        String,
  thingThatFailed: String,
  collection:      MongoCollection,
  query:           DBObject,
  wc:              WriteConcern,
  wr:              WriteResult
) extends Error("""

    %s: %s failed!

    Collection: %s
    WriteConcern: %s
    WriteResult: %s

    QUERY: %s

 """.format(whichDAO, thingThatFailed, collection.name, wc, wr, query))

case class SalatRemoveQueryError(
  whichDAO:   String,
  collection: MongoCollection,
  query:      DBObject,
  wc:         WriteConcern,
  wr:         WriteResult
) extends SalatDAOQueryError(whichDAO, "remove", collection, query, wc, wr)

case class SalatDAOUpdateError(
  whichDAO:   String,
  collection: MongoCollection,
  query:      DBObject,
  o:          DBObject,
  wc:         WriteConcern,
  wr:         WriteResult,
  upsert:     Boolean,
  multi:      Boolean
) extends Error("""

    %s: update failed!

    Collection: %s
    WriteConcern: %s
    WriteResult: %s
    Upsert: %s
    Multi: %s

    QUERY: %s

    OBJECT TO UPDATE: %s

 """.format(whichDAO, collection.name, wc, wr, upsert, multi, query, o))
