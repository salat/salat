*Salat* is a simple Scala serialization library that makes it easy to:

* serialize a Scala case class to a MongoDB `DBObject`

* deserialize a MongoDB `DBObject` back into an instance of the original case class

Documentation is [available on the wiki][wiki].

Feature highlights:

* Built to leverage 10gen's *Casbah* and its full suite of BSON
  encoders. Primitive values of types ranging from `Double` and
  `Float` to Joda Time's `DateTime` are supported seamlessly out of
  the box.

* Works with nested case class instances of arbitrary depth. Support
  is provided for nested `Seq`-s and `Map`-s, whose values may be
  primitives or embedded case classes which will also be
  (de)serialized.

* Supports Scala idioms such as `Option`-s and default argument values.

* Only minimal use is made of Java reflection. Where feasible, Salat leverages Scala class signatures.

*Salat* is [free software][github-link]. Its availability is governed
by the Apache 2 license. See [LICENSE.md][license] at source root for
more information. Please make use of the [GitHub project][github-link]
to report issues or contact the author.

- Source code: <https://github.com/novus/salat>
- Wiki: <https://github.com/novus/salat/wiki>
- Group: <http://groups.google.com/group/scala-salat>
- Repository: <http://repo.novus.com/releases/> and <http://repo.novus.com/snapshots/>

Development supported by [Novus Partners, Inc][novus].

[novus]: https://www.novus.com/
[github-link]: https://github.com/novus/salat
[license]: https://github.com/novus/salat/blob/master/README.md
[wiki]: https://github.com/novus/salat/wiki
