import sbt._

class SalatProject(info: ProjectInfo) extends DefaultProject(info) with posterous.Publish {
  override def managedStyle = ManagedStyle.Maven
  override def compileOptions = super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)

  val mongodb = "org.mongodb" % "mongo-java-driver" % "2.4"
  val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.0rc3"
  val scalap = "org.scala-lang" % "scalap" % "2.8.1"

  val specs = "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.0" % "test->default"

  val publishTo = Resolver.sftp("repobum", "repobum", "/home/public/%s".format(
    if (projectVersion.value.toString.endsWith("-SNAPSHOT")) "snapshots"
    else "releases"
  )) as("repobum_repobum", new java.io.File(Path.userHome + "/.ssh/repobum"))

  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"
}
