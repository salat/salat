# Salat

This project is an attempt to implement a *fast* MongoDB <-> Scala
**case class mapping mechanism**. It is in fact my third stab at this
problem; this time, I'm focusing on speed more than flexibility,
compatibility, and functionality.

## Goals

1. Convert an instance of *any* Scala case class to a `DBObject`
suitable for insertion into a MongoDB database.

2. Given a `DBObject` (which may have come from MongoDB, or
elsewhere), instantiate an instance of the corresponding case class.

3. Achieve goals **1** and **2** without sacrificing performance. This
more or less rules out use of reflection.

4. Achieve goals **1**, **2**, and **3** while adhering as much as
feasible to core tenets of functional programming.

## Goats sacrificed to the gods of performance

Considering above goals (especially **3**), I have decided to make
particular sacrifices and not look back.

### No support for anything but case classes

Scala case classes are essentially products, and as such provide
certain features which can be used to improve object
serialization. These features include:

- A simple and accessible constructor. Can be retrieved either by
  reflecting on the class itself, or through generated companion
  object.

- Provided `productIterator`: a non-reflective way to retrieve
  programmatically values of the case class' data members.

- Data members can have default values; information missing in
  `DBObject` instance during re-inflation can be subbed in using
  default args defined at compile time.

### Requires ScalaSig

I hereby thank EPFL for the wonderfully undocumented
ScalaSig. Fortunately, its source code reads like a work of art and
isn't hard to comprehend. Obviously, if the previous section hasn't
ruled out Java support, this certainly does.

ScalaSig does not appear to digest very well classes written in pure
Java. Not a big deal, because we only support case classes anyway.

### Flexibility can wait

There's currently no way to alter default behavior of Salat's critical
internal components. For example, it's impossible to tell it to
exclude particular data members from (de)serialization. Similarly, the
code is also not set up to even know anything about fields that aren't
part of the case class constructor.

## What's up with the name?

"*Salat*" is a transliteration of the Russian word "салат", for
"salad". I picked this name because salad is great for your health and
will not slow you down through use of runtime reflection.
