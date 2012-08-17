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