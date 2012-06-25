/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         MissingGraterExplanationSpec.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */
//package com.novus.salat.test.util
//
//import com.novus.salat._
//import com.novus.salat.test.global._
//import com.novus.salat.test.model._
//import com.mongodb.casbah.Imports._
//import com.novus.salat.test._
//import com.mongodb.util.JSON
//import com.novus.salat.util.{GraterGlitch, GraterFromDboGlitch}
//
//class MissingGraterExplanationSpec extends SalatSpec {
//  "Missing grater explanation" should {
//    "handle type hint glitches in DBOs" in {
//      "explain when a type hint refers to a class that can't be found" in {
//        // type hint "com.novus.salat.test.model.DoesNotExist" refers to non-existent class
//        val badCandy = """{ "_typeHint" : "com.novus.salat.test.model.ManyMauds" ,
//          "mauds" : [ { "_typeHint" : "com.novus.salat.test.model.DoesNotExist" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
//          { "_typeHint" : "com.novus.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.novus.salat.test.model.Ida" , "lake" : 8.0}}]}"""
//        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterGlitch]
//      }
////      "explain when a type hint refers to a trait" in {
////        // type hint "com.novus.salat.test.model.MaudLike" refers to trait
////        val badCandy = """{ "_typeHint" : "com.novus.salat.test.model.ManyMauds" ,
////          "mauds" : [ { "_typeHint" : "com.novus.salat.test.model.MaudLike" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
////          { "_typeHint" : "com.novus.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.novus.salat.test.model.Ida" , "lake" : 8.0}}]}"""
////        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterFromDboGlitch]
////      }
////      "explain when a type hint refers to an abstract class" in {
////        // type hint "com.novus.salat.test.model.Vertebrate" refers to abstract class
////        val badCandy = """{ "_typeHint" : "com.novus.salat.test.model.ManyMauds" ,
////          "mauds" : [ { "_typeHint" : "com.novus.salat.test.model.Vertebrate" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
////          { "_typeHint" : "com.novus.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.novus.salat.test.model.Ida" , "lake" : 8.0}}]}"""
////        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterFromDboGlitch]
////      }
//      "explain when a type hint refers to a class that is not a case class" in {
//        // type hint "com.novus.salat.test.model.NotACaseClass" refers to something that is...  not a case class!
//        val badCandy = """{ "_typeHint" : "com.novus.salat.test.model.NotACaseClass" ,
//          "mauds" : [ { "_typeHint" : "com.novus.salat.test.model.Vertebrate" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
//          { "_typeHint" : "com.novus.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.novus.salat.test.model.Ida" , "lake" : 8.0}}]}"""
//        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterFromDboGlitch]
//      }
//    }
//  }
//}