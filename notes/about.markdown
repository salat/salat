*Salat* is a simple Scala serialization library that makes it easy to
produce a MongoDB `DBObject` from any Scala case class. It is
similarly easy to re-inflate said `DBObject` back into an instance of
the original case class.

Documentation is [available on the wiki][wiki].

Feature highlights:

* Built on 10gen's *Casbah* and can leverage its full suite of BSON
  encoders. Primitive values of types ranging from `Double` and
  `Float` to Joda Time's `DateTime` are supported seamlessly out of
  the box.

* Works with nested case class instances of arbitrary depth. Support
  is provided for nested `Seq`-s and `Map`-s, whose values may be
  primitives or embedded case classes which will also be
  (de)serialized.

* Supports Scala idioms such as `Option`-s and default argument values.

* Only minimal use is made of Java reflection. Where possible,
  intrinsic properties of Scala case classes are leveraged as much as
  feasible.

*Salat* is [free software][github-link]. Its availability is governed
by the MIT license. See [LICENSE.md][license] at source root for more
information. Please make use of the [GitHub project][github-link] to
report issues or contact the author.

[github-link]: https://github.com/novus/salat
[license]: https://github.com/novus/salat/blob/master/README.md
[wiki]: https://github.com/novus/salat/wiki
