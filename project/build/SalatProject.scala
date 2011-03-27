import sbt._

class SalatProject(info: ProjectInfo) extends ParentProject(info) with posterous.Publish {
  //override def defaultModuleSettings = inlineSettings
  override def managedStyle = ManagedStyle.Maven

  lazy val core = project("salat-core", "salat-core", new SalatCoreProject(_))
  lazy val proto = project("salat-proto", "salat-proto", new SalatProtoProject(_), core)

  val allSource: PathFinder = core.mainSourcePath ** "*.scala" +++
                              proto.mainSourcePath ** "*.scala"

  abstract class BaseSalatProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = super.compileOptions ++ Seq(Unchecked, Deprecation)

    val specs2 = "org.specs2" %% "specs2" % "1.1-SNAPSHOT" % "test" withSources()
    val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test->default" withSources()
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.0" % "test->default" withSources()

    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")

    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)

    lazy val sourceArtifact = Artifact.sources(artifactID)
    lazy val docsArtifact = Artifact.javadoc(artifactID)
    override def packageSrcJar = defaultJarPath("-sources.jar")
    override def packageDocsJar = defaultJarPath("-javadoc.jar")
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)
  }

  class SalatCoreProject(info: ProjectInfo) extends BaseSalatProject(info) {
    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.5.2" withSources()
    val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.0.3" withSources()
    val commons_pool = "commons-pool" % "commons-pool" % "1.5.5"

    // Should be crossScalaVersionString, but 2.8.0's scalap appears to
    // be totally frakked, whereas 2.8.1's works fine with 2.8.0. Go
    // figure.
    val scalap = "org.scala-lang" % "scalap" % "2.8.1" withSources()
  }

  class SalatProtoProject(info: ProjectInfo) extends SalatCoreProject(info) with protobuf.ProtobufCompiler {
    val protobuf = "com.google.protobuf" % "protobuf-java" % "2.3.0" withSources()

    override def protobufDirectory = "src" / "test" / "protobuf"

    override def protobufOutputPath = "src" / "test" / "java"

//    override def generateProtobufAction = task {
//      val mostRecentSchemaTimestamp = protobufSchemas.get.map {
//        _.asFile.lastModified
//      }.toList.sort {
//        _ > _
//      }.head
//      log.info("mostRecentSchemaTimestamp: %s".format(mostRecentSchemaTimestamp))
//      log.info("protobufOutputPath.asFile.lastModified=%s".format(protobufOutputPath.asFile.lastModified))
//
//      if (mostRecentSchemaTimestamp >= protobufOutputPath.asFile.lastModified) {
//        for (schema <- protobufSchemas.get) {
//          log.info("Compiling schema %s".format(schema))
//        }
//        protobufOutputPath.asFile.mkdirs()
//        log.info("protobufOutputPath: %s".format(protobufOutputPath))
//        val incPath = protobufIncludePath.map(_.absolutePath).mkString("-I ", " -I ", "")
//        log.info("incPath: %s".format(incPath))
//        log.info("""ABOUT TO RUN:
//        protoc %s --java_out=%s%s
//        """.format(incPath, protobufOutputPath.absolutePath, protobufSchemas.getPaths.mkString(" ")))
//        <x>protoc {incPath} --java_out={protobufOutputPath.absolutePath} {protobufSchemas.getPaths.mkString(" ")}</x> ! log
//        protobufOutputPath.asFile.setLastModified(mostRecentSchemaTimestamp)
//      }
//
//      None
//    } describedAs ("Generates Java classes from the specified Protobuf schema files.")

    override protected def testCompileAction = {
//      log.info("testCompileAction: preparing to generate...")
      super.testCompileAction dependsOn (generateProtobuf)
    }

    // I only generate protobuf messages for testing, so we don't need to hook this in to compile
    override def compileAction = super.compileAction

//    override def cleanAction = super.cleanAction dependsOn(cleanProtobuf)
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
