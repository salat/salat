import sbt._

class SalatProject(info: ProjectInfo) extends ParentProject(info) with posterous.Publish {
  //override def defaultModuleSettings = inlineSettings
  override def managedStyle = ManagedStyle.Maven

  lazy val core = project("salat-core", "salat-core", new SalatCoreProject(_))

  abstract class BaseSalatProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = super.compileOptions ++ Seq(Unchecked, Deprecation)

    val specsVersion = crossScalaVersionString match {
      case "2.8.0" => "1.6.5"
      case "2.8.1" => "1.6.7"
    }
    val specs = "org.scala-tools.testing" %% "specs" % specsVersion % "test->default" withSources()

    val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test->default" withSources()
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.0" % "test->default" withSources()

    override def packageSrcJar= defaultJarPath("-sources.jar")
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)
  }

  class SalatCoreProject(info: ProjectInfo) extends BaseSalatProject(info) {
    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.4" withSources()
    val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.0.3" withSources()
    val commons_pool = "commons-pool" % "commons-pool" % "1.5.5"

    // Should be crossScalaVersionString, but 2.8.0's scalap appears to
    // be totally frakked, whereas 2.8.1's works fine with 2.8.0. Go
    // figure.
    val scalap = "org.scala-lang" % "scalap" % "2.8.1" withSources()
  }

  val publishTo = Resolver.sftp("repo.novus.com", "repo.novus.com", "/nv/repo/%s".format(
    if (projectVersion.value.toString.endsWith("-SNAPSHOT")) "snapshots"
    else "releases"
  )) as (System.getProperty("user.name"))

  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val novusRepo = "Novus Release Repository" at "http://repo.novus.com/releases/"
  val novusSnapsRepo = "Novus Snapshots Repository" at "http://repo.novus.com/snapshots/"
  val mothership = "Maven Repo1" at "http://repo1.maven.org/maven2/"
}
