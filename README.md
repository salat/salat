# Salat

[![Build Status](https://travis-ci.org/salat/salat.svg?branch=1.11.x-branch)](https://travis-ci.org/salat/salat)

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

Available for Scala 2.10, 2.11 and 2.12. Based on Casbah 3.1.1, with support for Mongo 3.x

    "com.github.salat" %% "salat" % "1.11.2"

[Release Notes](https://github.com/salat/salat/blob/1.11.x-branch/notes/1.11.2.markdown)

## Snapshot

Available for Scala 2.11 and 2.12. Based on Casbah 3.1.1, with support for Mongo 3.x

    "com.github.salat" %% "salat" % "1.11.3-SNAPSHOT"

[SNAPSHOT Release Notes](https://github.com/salat/salat/blob/1.11.x-branch/notes/1.11.3.markdown)

## Legacy support

### Package

Starting with version 1.10.0, the package for Salat has changed.

**Version 1.10.x and Later**

`import salat._`

**Version 1.9.x and Earlier**

`import com.novus.salat._`

### Repositories

Salat has been hosted exclusively by Sonatype since version 0.0.8. Please remove all references to `repo.novus.com` from your build files.

If you are not using sbt 0.11.2+, or you need a SNAPSHOT release, explicitly add OSS Sonatype to your resolvers:

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

### Scala 2.12.x

Based on Casbah 3.1.1.

    "org.github.salat" %% "salat" % "1.11.2"

### Scala 2.11.x

Based on Casbah 3.1.1.

    "org.github.salat" %% "salat" % "1.11.2"

Based on Casbah 2.8.2.

    "org.github.salat" %% "salat" % "1.10.0"

### Scala 2.10.4

Based on Casbah 2.7.1.

    "com.novus" %% "salat" % "1.9.10"

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

Are you using Play framework?  Make sure to see our [Play support][play-salat] wiki page, and check out the play-salat plugin at [cloudinsights/play-salat][play-salat-plugin].

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
[play-salat-plugin]: https://github.com/cloudinsights/play-salat
[sid10]: http://www.scala-lang.org/sid/10
[typehint]: https://github.com/salat/salat/wiki/TypeHints
[collections]: https://github.com/salat/salat/wiki/Collections
