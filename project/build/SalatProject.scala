import sbt._

class SalatProject(info: ProjectInfo) extends DefaultProject(info) with posterous.Publish {
  override def managedStyle = ManagedStyle.Maven

  override def compileOptions = super.compileOptions ++ Seq(Unchecked, Deprecation)

  val mongodb = "org.mongodb" % "mongo-java-driver" % "2.4"
  val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.0.1"

  // Should be crossScalaVersionString, but 2.8.0's scalap appears to
  // be totally frakked, whereas 2.8.1's works fine with 2.8.0. Go
  // figure.
  val scalap = "org.scala-lang" % "scalap" % "2.8.1"

  val specsVersion = crossScalaVersionString match {
    case "2.8.0" => "1.6.5"
    case "2.8.1" => "1.6.6"
  }
  val specs = "org.scala-tools.testing" %% "specs" % specsVersion % "test->default"

  val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test->default"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.0" % "test->default"

  val publishTo = Resolver.sftp("repo.novus.com", "repo.novus.com", "/nv/repo/%s".format(
    if (projectVersion.value.toString.endsWith("-SNAPSHOT")) "snapshots"
    else "releases"
  )) as (System.getProperty("user.name"))

  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"
}
