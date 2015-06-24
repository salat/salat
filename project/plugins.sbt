resolvers ++= Seq(
  Resolver.url("sbt-plugin-releases2", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("com.danieltrinh" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
