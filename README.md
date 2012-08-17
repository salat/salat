# Note:

This project is a fork of novus/salat.  There are one key difference between the project and one bug fixed.
The bug fixed is that the original code goes boom if you try to deserialize a JSON Map[String,Enumeration.Value].  Certain uses of
deserialized object crash with a ClassCastException.  This has been fixed in this code.

The big difference between the two project is that the gzoller/salat version ignores the @Key annotation for JSON but 
recognizes it for Mongo serialization.  The idea here is that for JSON serialization you're using that JSON in a various
ways, including sending to a GUI, and you probably don't want your key field called '_id', but rather want it called
whatever you named the field originally.  But... for Mongo you do want to rename the field '_id'.  This code recognizes
this difference.

I've had some email exchanges with Rose and there's a good chance future novus/salat releases may have a more flexible
way to do what I've done here, but for now those who need this feature are welcome to use this code.  I don't claim to
track the original project perfectly but I do update it from time to time.

Cheers.

# Salat

Salat is a simple serialization library for case classes.

Salat currently supports bidirectional serialization for:

- MongoDB's `DBObject` (using [casbah][casbah])
- JSON (using [lift-json][lift-json])
- maps

# Goals

Simplicity.  Flexibility.  Consistency.

Your model there and back again should just work.

# Get Salat

Salat publishes snapshots and releases to OSS Sontatype.

Please remove all references to `repo.novus.com` from your build files.  After 0.0.8, Salat will be hosted exclusively by Sonatype.

## Stable Release

Available for Scala 2.9.1 and 2.9.2.  Based on Casbah 2.4.1.

    "com.novus" %% "salat" % "1.9.0"

[Release Notes](http://notes.implicit.ly/post/26652751811/salat-1-9-0)

Available for Scala 2.8.1, 2.9.1 and 2.9.2.  Based on Casbah 2.1.5-1.

    "com.novus" %% "salat" % "0.0.8"

[Release Notes](http://notes.implicit.ly/post/25793638048/salat-0-0-8)

## Snapshot

[![Build Status](https://secure.travis-ci.org/novus/salat.png)](http://travis-ci.org/novus/salat)  Available for Scala 2.9.1 and 2.9.2.

    "com.novus" %% "salat" % "1.9.1-SNAPSHOT"

If you are not using sbt 0.11.2+, explicitly add OSS Sonatype to your resolvers:

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

[Release Notes (In Progress)](https://github.com/novus/salat/blob/master/notes/1.9.1.markdown)

## Play 2 plugin

Are you using Play framework?  Make sure to see our [Play support][play-salat] wiki page, and check out Leon Radley's plugin at [leon/play-salat][play-salat-plugin].

# Documentation

See the [wiki][wiki] and the [mailing list][group].

# What does Salat support?

See [Supported Types][types].

# What doesn't Salat support?

We don't have the resources to support everything.  Here are some things Salat doesn't do:

- Java compatibility
- non-case classes
- type aliases
- nested inner classes
- varags
- arrays
- multiple constructors
- tuples
- `Option` containing a collection (see [collection support][collections] for workarounds)
- relationship management like a traditional ORM

# How does Salat work?

Salat uses the `Product` trait implemented by case classes with the hi-fi type information found in pickled Scala signatures.

Details are thin on the ground, but here's where we got started:

- the source code for `scala.tools.scalap.scalax.rules.scalasig.ScalaSigParser`
- SID # 10 (draft) - [Storage of pickled Scala signatures in class files][sid10]

[types]: https://github.com/novus/salat/wiki/SupportedTypes
[wiki]: https://github.com/novus/salat/wiki
[casbah]: https://github.com/mongodb/casbah/
[lift-json]: https://github.com/lift/lift/tree/master/framework/lift-base/lift-json/
[group]: http://groups.google.com/group/scala-salat
[play-salat]: https://github.com/novus/salat/wiki/SalatWithPlay2
[play-salat-plugin]: https://github.com/leon/play-salat
[sid10]: http://www.scala-lang.org/sid/10
[typehint]: https://github.com/novus/salat/wiki/TypeHints
[collections]: https://github.com/novus/salat/wiki/Collections