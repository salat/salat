/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         MissingGraterExplanationSpec.scala
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
//package com.github.salat.test.util
//
//import com.github.salat._
//import com.github.salat.test.global._
//import com.github.salat.test.model._
//import com.mongodb.casbah.Imports._
//import com.github.salat.test._
//import com.mongodb.util.JSON
//import com.github.salat.util.{GraterGlitch, GraterFromDboGlitch}
//
//class MissingGraterExplanationSpec extends SalatSpec {
//  "Missing grater explanation" should {
//    "handle type hint glitches in DBOs" in {
//      "explain when a type hint refers to a class that can't be found" in {
//        // type hint "com.github.salat.test.model.DoesNotExist" refers to non-existent class
//        val badCandy = """{ "_typeHint" : "com.github.salat.test.model.ManyMauds" ,
//          "mauds" : [ { "_typeHint" : "com.github.salat.test.model.DoesNotExist" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
//          { "_typeHint" : "com.github.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.github.salat.test.model.Ida" , "lake" : 8.0}}]}"""
//        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterGlitch]
//      }
////      "explain when a type hint refers to a trait" in {
////        // type hint "com.github.salat.test.model.MaudLike" refers to trait
////        val badCandy = """{ "_typeHint" : "com.github.salat.test.model.ManyMauds" ,
////          "mauds" : [ { "_typeHint" : "com.github.salat.test.model.MaudLike" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
////          { "_typeHint" : "com.github.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.github.salat.test.model.Ida" , "lake" : 8.0}}]}"""
////        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterFromDboGlitch]
////      }
////      "explain when a type hint refers to an abstract class" in {
////        // type hint "com.github.salat.test.model.Vertebrate" refers to abstract class
////        val badCandy = """{ "_typeHint" : "com.github.salat.test.model.ManyMauds" ,
////          "mauds" : [ { "_typeHint" : "com.github.salat.test.model.Vertebrate" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
////          { "_typeHint" : "com.github.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.github.salat.test.model.Ida" , "lake" : 8.0}}]}"""
////        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterFromDboGlitch]
////      }
//      "explain when a type hint refers to a class that is not a case class" in {
//        // type hint "com.github.salat.test.model.NotACaseClass" refers to something that is...  not a case class!
//        val badCandy = """{ "_typeHint" : "com.github.salat.test.model.NotACaseClass" ,
//          "mauds" : [ { "_typeHint" : "com.github.salat.test.model.Vertebrate" , "swept" : "swept" , "out" : "out" , "toSea" : "tuo tpews"} ,
//          { "_typeHint" : "com.github.salat.test.model.Maud6" , "swept" : "swept" , "out" : "out" , "ida" : { "_typeHint" : "com.github.salat.test.model.Ida" , "lake" : 8.0}}]}"""
//        grater[ManyMauds].asObject(JSON.parse(badCandy).asInstanceOf[DBObject]) must throwA[GraterFromDboGlitch]
//      }
//    }
//  }
//}
