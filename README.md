# Salat

[![Build Status](https://travis-ci.org/salat/salat.svg?branch=master)](https://travis-ci.org/salat/salat)

Salat is a simple serialization library for case classes.

Salat currently supports bidirectional serialization for:

- MongoDB's `DBObject` (using [casbah][casbah])
- JSON (using [JSON4S][JSON4S])
- maps

# Goals

Simplicity.  Flexibility.  Consistency.

Your model there and back again should just work.

# Get Salat

Salat publishes snapshots and releases to OSS Sontatype.

## Stable Release

Available for Scala 2.10 and 2.11. Based on Casbah 2.7.1.

    "com.novus" %% "salat" % "1.9.9"

[Release Notes](https://github.com/novus/salat/blob/master/notes/1.9.9.markdown)

## Snapshot

[![Build Status](https://secure.travis-ci.org/novus/salat.png)](http://travis-ci.org/novus/salat)

Available for Scala 2.10 and 2.11. Based on Casbah 2.7.1.

    "com.novus" %% "salat" % "2.0.0-SNAPSHOT"

[Release Notes (In Progress)](https://github.com/novus/salat/blob/master/notes/2.0.0.markdown)

## Legacy support

Please remove all references to `repo.novus.com` from your build files.  After 0.0.8, Salat will be hosted exclusively by Sonatype.

If you are not using sbt 0.11.2+, explicitly add OSS Sonatype to your resolvers:

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

### Scala 2.9.3

Based on Casbah 2.7.0.

    "com.novus" %% "salat" % "1.9.7"

### Scala 2.9.2

Based on Casbah 2.6.4.

    "com.novus" %% "salat" % "1.9.5"

### Scala 2.8.1

Based on Casbah 2.1.5-1.

    "com.novus" %% "salat" % "0.0.8"

[Release Notes](http://notes.implicit.ly/post/25793638048/salat-0-0-8)

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

[types]: https://github.com/salat/salat/wiki/SupportedTypes
[wiki]: https://github.com/salat/salat/wiki
[casbah]: https://github.com/mongodb/casbah/
[JSON4S]: http://json4s.org/
[group]: http://groups.google.com/group/scala-salat
[play-salat]: https://github.com/salat/salat/wiki/SalatWithPlay2
[play-salat-plugin]: https://github.com/leon/play-salat
[sid10]: http://www.scala-lang.org/sid/10
[typehint]: https://github.com/salat/salat/wiki/TypeHints
[collections]: https://github.com/salat/salat/wiki/Collections
