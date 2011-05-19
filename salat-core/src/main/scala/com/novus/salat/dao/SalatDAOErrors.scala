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
  *
  */
package com.novus.salat.dao

import com.mongodb.{DBObject, WriteConcern}
import com.mongodb.casbah.TypeImports._

abstract class SalatDAOError(whichDAO: String,
                             thingThatFailed: String,
                             collection: MongoCollection,
                             wc: WriteConcern,
                             wr: WriteResult,
                             dbos: List[DBObject]) extends Error("""

    %s: %s failed!

    Collection: %s
    WriteConcern: %s
    WriteResult: %s

    FAILED TO %s %s
    %s


 """.format(whichDAO, thingThatFailed,
  collection.getName(), wc, wr,
  thingThatFailed.toUpperCase(),
  if (dbos.size == 1) "DBO" else "DBOs",
  if (dbos.size == 1) dbos.head else dbos.mkString("\n")))

case class SalatInsertError(description: String,
                       collection: MongoCollection,
                       wc: WriteConcern,
                       wr: WriteResult,
                       dbos: List[DBObject]) extends SalatDAOError(description, "insert", collection, wc, wr, dbos)

case class SalatRemoveError(description: String,
                       collection: MongoCollection,
                       wc: WriteConcern,
                       wr: WriteResult,
                       dbos: List[DBObject]) extends SalatDAOError(description, "remove", collection, wc, wr, dbos)

case class SalatSaveError(description: String,
                     collection: MongoCollection,
                     wc: WriteConcern,
                     wr: WriteResult,
                     dbos: List[DBObject]) extends SalatDAOError(description, "save", collection, wc, wr, dbos)

abstract class SalatDAOQueryError(whichDAO: String,
                                  thingThatFailed: String,
                                  collection: MongoCollection,
                                  query: DBObject,
                                  wc: WriteConcern,
                                  wr: WriteResult) extends Error("""

    %s: %s failed!

    Collection: %s
    WriteConcern: %s
    WriteResult: %s

    QUERY: %s

 """.format(whichDAO, thingThatFailed, collection.getName(), wc, wr, query))

case class SalatRemoveQueryError(whichDAO: String,
                            collection: MongoCollection,
                            query: DBObject,
                            wc: WriteConcern,
                            wr: WriteResult) extends SalatDAOQueryError(whichDAO, "remove", collection, query, wc, wr)

case class SalatDAOUpdateError(whichDAO: String,
                          collection: MongoCollection,
                          query: DBObject,
                          o: DBObject,
                          wc: WriteConcern,
                          wr: WriteResult,
                          upsert: Boolean,
                          multi: Boolean) extends Error("""

    %s: update failed!

    Collection: %s
    WriteConcern: %s
    WriteResult: %s
    Upsert: %s
    Multi: %s

    QUERY: %s

    OBJECT TO UPDATE: %s

 """.format(whichDAO, collection.getName(), wc, wr, upsert, multi, query, o))