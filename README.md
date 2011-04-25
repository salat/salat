# Salat

[Salat in four minutes... or less][lightning-talk]

Salat is a bi-directional Scala case class serialization library that leverages
MongoDB's `DBObject` (which uses BSON underneath) as its target format.

The Salat project focuses on **speed** and **simplicity** of serializing case classes to and from target formats.

Salat is not a fully-fledged ORM and does not attempt to match the flexibility, compability or functionality of an ORM
that would let you define relationships between classes, provide a query language, or serialize/deserialize every collection
type known to Java or Scala.

- Documentation available on our [wiki][wiki].
- Salat-related discussion and questions belong on the [mailing list][mailing-list].
- Follow [Rose][rktoomey] on Twitter - [@prasinous][rkt-twitter]

## Goals

1. Convert an instance of *any* Scala case class to a `DBObject`
suitable for insertion into a MongoDB database.

2. Given a `DBObject` (which may have come from MongoDB, or
elsewhere), instantiate an instance of the corresponding case class.

3. Achieve goals **1** and **2** without sacrificing performance. This rules out use of reflection.

4. Achieve goals **1**, **2**, and **3** as functionally as possible.

## What are you giving up?

In developing Salat, we have made a key decision to make these particular sacrifices and not look back.

### No support for anything but case classes

Scala case classes have key features that Salat uses to improve and speed up object serialization, including:

- A single accessible constructor. Can be retrieved either by reflecting on the class itself, or through generated companion
  object.

- The `productIterator` method provided by the `Product` interface: a non-reflective way to retrieve
  programmatically values of the case class' data members.

- Data members can have default values; information missing in
  `DBObject` instance during re-inflation can be subbed in using
  default args defined at compile time.

### Requires ScalaSig

So the first big improvement Salat offers for serialization is in being able to use case classes as products to get a
list of indexed fields and their default arguments.

Well begun, but only half done.  The other half is getting as much type information about the case class as possible, and
then keeping that information on tap instead of having to derive it over and over again.

With these two pieces in hand, fast, reliable serialization without having to resort to runtime reflection is easy.

So thank you to [EFPL][efpl] for their wonderfully undocumented `ScalaSig`, which Salat uses to memoize **hi-fi type
information** about each field in the case class.

Details about pickled Scala sigs are thin on the ground.  Here's where we got started:

- the source code for `scala.tools.scalap.scalax.rules.scalasig.ScalaSigParser`
- [SID # 10 (draft) - Storage of pickled Scala signatures in class files][http://www.scala-lang.org/sid/10]

#### Java support has been ruled out

Requiring `Product` and a single constructor rules out using anything but a case class.  Technically, in the distant future,
we may be able to flex on the case class requirement enough to only require something very, very case class like.

However, Salat will never support classes defined in pure Java because they do not supply

#### You can't freestyle in the REPL either

*NB*: it turns out that ScalaSig is incapable of "parsing" classes defined in the REPL.

 **None of this code will work with classes that have no corresponding `.class` file.**

 We're hoping that this will be remedied with Scala 2.9, but until then, if you want to experiment with serialization in the REPL:
 - define your model classes
 - run `sbt console`

### Flexibility can wait

Salat is and will always be primarily focused on serializing and deserializing what's in the case class constructor.

There are some annotations that can be used to customize serialization at the case class level:

- Support for typing concrete case class instances to a trait or abstract superclass using `@Salat`
- Use `@Key` to change a key name
- Use `@Persist` to persist a value not in case class constructor
- Use `@Ignore` to ignore a field in the case class constructor (requires default value)
- Use `@EnumAs` to choose whether to handle enums by id or by value

In addition, you can customize your persistence context with regards to:

 - type hinting strategy
 - type hinting key name
 - global key overrides
 - global enum handling strategy
 - default math context for BigDecimals

However, a lot of Salat's internal workings are not easily exposed or overriden right now.  We are working to make Salat
a more general-use serialization tool with future releases.


## What's up with the name?

"*Salat*" is a transliteration of the Russian word "салат", for
"salad".

Salat is light and doesn't slow you down through use of runtime reflection.

[wiki]: https://github.com/novus/salat/wiki
[mailing-list]: http://groups.google.com/group/scala-salat
[lightning-talk]: http://repo.novus.com/salat-presentation
[rkt-twitter]: http://twitter.com/prasinous
[rktoomey]: https://github.com/rktoomey
[efpl]: http://www.epfl.ch/