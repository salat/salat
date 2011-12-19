import sbt._
import Keys._
import ScalariformPlugin.{ format, formatPreferences }

object SalatBuild extends Build {

  import Dependencies._
  import BuildSettings._

  val utilDeps = Seq(specs2, slf4jApi, slf4jSimple)
  val coreDeps = Seq(mongoJava, casbah_core, lift_json, commonsLang, specs2)

  lazy val salat = Project(
    id = "salat",
    base = file("."),
    settings = buildSettings ++ Seq(
      publishArtifact := false
    ),
    aggregate = Seq(util, core)
  ) dependsOn(util, core)

  lazy val util = {
    val id = "salat-util"
    val base = file("salat-util")
    val settings = buildSettings ++ Seq(
      libraryDependencies ++= utilDeps,
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _)
    )
    Project(id = id, base = base, settings = settings)
  }

  lazy val core = Project(
    id = "salat-core",
    base = file("salat-core"),
    settings = buildSettings ++ Seq(libraryDependencies ++= coreDeps)
  ) dependsOn (util)

}

object BuildSettings {

  import Repos._

  val buildOrganization = "com.novus"
  val buildVersion = "0.0.8-SNAPSHOT"
  val buildScalaVersion = "2.8.1"

  val buildSettings = Defaults.defaultSettings ++ Format.settings ++ Publish.settings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
    parallelExecution in Test := false,
    testFrameworks += TestFrameworks.Specs2,
    resolvers ++= Seq(scalaToolsRepo, scalaToolsSnapRepo, novusRepo, novusSnapsRepo, typeSafeRepo),
    scalacOptions ++= Seq("-deprecation", "-unchecked")
  )
}

object Format {
  lazy val settings = ScalariformPlugin.settings ++ Seq(
    formatPreferences in Compile := formattingPreferences,
    formatPreferences in Test    := formattingPreferences
  )

  private def formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences().setPreference(AlignParameters, true).
      setPreference(AlignSingleLineCaseStatements, true).
      setPreference(CompactControlReadability, true). // waiting for CCR patch to go mainstream, patiently patiently
      setPreference(CompactStringConcatenation, true).
      setPreference(DoubleIndentClassDeclaration, true).
      setPreference(FormatXml, true).
      setPreference(IndentLocalDefs, true).
      setPreference(IndentPackageBlocks, true).
      setPreference(IndentSpaces, 2).
      setPreference(MultilineScaladocCommentsStartOnFirstLine, true).
      setPreference(PreserveSpaceBeforeArguments, false).
      setPreference(PreserveDanglingCloseParenthesis, false).
      setPreference(RewriteArrowSymbols, false).
      setPreference(SpaceBeforeColon, false).
      setPreference(SpaceInsideBrackets, false).
      setPreference(SpacesWithinPatternBinders, true)
  }
}

object Publish {
  lazy val settings = Seq(
    publishTo <<= (version) {
      version: String =>
        val r = Resolver.sftp("repo.novus.com", "repo.novus.com", "/nv/repo/%s".format(
          if (version.trim().toString.endsWith("-SNAPSHOT")) "snapshots" else "releases"
        )) as (System.getProperty("user.name"))
        Some(r)
    }
  )
}

object Dependencies {
  val specs2 = "org.specs2" %% "specs2" % "1.5" % "test"
  val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.4"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.4" % "test"
  val mongoJava = "org.mongodb" % "mongo-java-driver" % "2.5.3"
  val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.1.5.0"
  val lift_json = "net.liftweb" %% "lift-json" % "2.4-M4"
}

object Repos {
  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val novusRepo = "Novus Release Repository" at "http://repo.novus.com/releases/"
  val novusSnapsRepo = "Novus Snapshots Repository" at "http://repo.novus.com/snapshots/"
  val typeSafeRepo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
}

// Shell prompt which show the current project, git branch and build version
object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: => String) {}

    def error(s: => String) {}

    def buffer[T](f: => T): T = f
  }

  val current = """\*\s+([\w-]+)""".r

  def gitBranches = ("git branch --no-color" lines_! devnull mkString)

  val buildShellPrompt = {
    (state: State) => {
      val currBranch =
        current findFirstMatchIn gitBranches map (_ group (1)) getOrElse "-"
      val currProject = Project.extract(state).currentProject.id
      "%s:%s:%s> ".format(
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

