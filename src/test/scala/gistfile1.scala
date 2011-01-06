package com.agemooij.test

import java.util.Date

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import com.mongodb.casbah.Imports._
import com.bumnetworks.salat._
import com.bumnetworks.salat.global._

case class Tweet (
    val id: Long,
    val user: String,
    val mentions: List[String] = Nil
)

case class Item (
    val src: String,
    val subtype: String,
    val createdAt: Date,
    val addedAt: Date,
    val text: String,
    val links: List[String] = Nil,
    val tags: List[String] = Nil,
    val users: List[String],
    val tweet: Option[Tweet] = None,
    val nrOfUsers: Int
)

@RunWith(classOf[JUnitRunner])
class ItemRepositoryTest extends FlatSpec with ShouldMatchers {

  grater[Tweet]
  grater[Item]

    "Serialization of a nested case class instance with Salat" should "work as expected" in {
        object ItemSerializer extends Grater(classOf[Item])
        
        val tweet = Tweet(id = 123456789L, user = "agemooij")
        val item = Item (
            src = "Twitter",
            subtype = "favorite",
            createdAt = new Date(),
            addedAt = new Date(),
            text = "the text",
            links = Nil,
            tags = List("tag1", "tag2", "tag3", "tag4"),
            users = List("agemooij", "snidag"),
            tweet = Some(tweet),
            nrOfUsers = 2
        )
        
        val itemAsMongo: MongoDBObject = ItemSerializer.asDBObject(item)
        
        println("Item as mongo:")
        println(itemAsMongo.asDBObject)
        println("Users list:")
        println(itemAsMongo.get("users"))
        
        assert(itemAsMongo.as[String]("src") === "Twitter")
        
        val itemDeserialized: Item = ItemSerializer.asObject(itemAsMongo)
        
        assert(item === itemDeserialized)
        assert(itemDeserialized.tweet === Some(tweet))
        
        itemDeserialized.tweet match {
            case Some(tweeeet) => {
                println("Tweeeet: " + tweet)
                
                assert(tweeeet === tweet)
                assert(tweeeet.id === 123456789L)
                assert(tweeeet.user === "agemooij")
                assert(tweeeet.mentions === Nil)
            }
            case _ => throw new IllegalStateException("The deserialized item did not contain a deserialized tweet.")
        }
    }
}

/*
Resulting output:

{ 
  "_typeHint" : "com.agemooij.test.Item" , 
  "src" : "Twitter" , 
  "subtype" : "favorite" , 
  "createdAt" : { "$date" : "2011-01-06T17:13:52Z"} , 
  "addedAt" : { "$date" : "2011-01-06T17:13:52Z"} , 
  "text" : "the text" , 
  "links" : [ ] , 
  "tags" : [ "tag1" , "tag2" , "tag3" , "tag4"] , 
  "users" : [ "agemooij" , "snidag"] , 
  "tweet" : [ 123456789 , "agemooij" , [ ]] ,   <=== huh ??
  "nrOfUsers" : 2
}
*/
