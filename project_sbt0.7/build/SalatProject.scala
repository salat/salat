import java.lang.Error
import sbt._

class SalatProject(info: ProjectInfo) extends ParentProject(info) with posterous.Publish {
  //override def defaultModuleSettings = inlineSettings
  override def managedStyle = ManagedStyle.Maven

  lazy val util = project("salat-util", "salat-util", new SalatCoreProject(_))
  lazy val core = project("salat-core", "salat-core", new SalatCoreProject(_), util)
//  lazy val proto = project("salat-proto", "salat-proto", new SalatProtoProject(_), core)

  val allSource: PathFinder = util.mainSourcePath ** "*.scala" +++ core.mainSourcePath ** "*.scala"

  abstract class BaseSalatProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = super.compileOptions ++ Seq(Unchecked, Deprecation)

    val specs2 = "org.specs2" %% "specs2" % "1.4" % "test"
    val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test->default"
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.0" % "test->default"

    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")

    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)

    lazy val sourceArtifact = Artifact.sources(artifactID)
//    lazy val docsArtifact = Artifact.javadoc(artifactID)
    override def packageSrcJar = defaultJarPath("-sources.jar")
//    override def packageDocsJar = defaultJarPath("-javadoc.jar")
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)
  }

  class SalatUtilProject(info: ProjectInfo) extends BaseSalatProject(info) {
    val scalap = "org.scala-lang" % "scalap" % crossScalaVersionString
  }

  class SalatCoreProject(info: ProjectInfo) extends SalatUtilProject(info) {

    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.5.3"
    val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.1.5.0"
    val commons_pool = "commons-pool" % "commons-pool" % "1.5.5"

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
