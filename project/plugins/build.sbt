// TODO: using custom build of sbt-scalariform plugin so I can use scalariform trunk with Novus CCR patch
// resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Novus Snapshots Repository" at "http://repo.novus.com/snapshots/"

// libraryDependencies += "com.typesafe" %% "sbt-scalariform" % "0.1.1"
libraryDependencies += "com.typesafe.sbt-scalariform" %% "sbt-scalariform" % "0.1.3.1-SNAPSHOT"
