package com.novus.salat.test.bson

case class People(country: String, residents: List[Person])

case class Person(age: Int, weight: Double, height: Int, name: String, attributes: List[String])