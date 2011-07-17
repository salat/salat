import sbt._
import Keys._

object SalatBuild extends Build {

  import Repos._
  import Dependencies._
  import BuildSettings._

  val utilDeps = Seq(specs2, commonsLang, slf4jSimple)
  val casbahDeps = Seq(mongoJava, commons_pool, casbah_core)

  lazy val salat = Project(
    id = "salat",
    base = file("."),
    settings = buildSettings ++ Seq(
      publishTo <<= (version) {
        version: String =>
          val r  = Resolver.sftp("repo.novus.com", "repo.novus.com", "/nv/repo/%s".format(
            if (version.trim().toString.endsWith("-SNAPSHOT")) "snapshots" else "releases"
          )) as (System.getProperty("user.name"))
        Some(r)
      }),
      aggregate = Seq(salatUtil, salatCore)
    )

  lazy val salatUtil = {
    val id = "salat-util"
    val base = file("salat-util")
    val settings = buildSettings ++ Seq(
      libraryDependencies ++= utilDeps,
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _)
    )
    Project(id = id, base = base, settings = settings)
  }

  lazy val salatCore = Project(
    id = "salat-core",
    base = file("salat-core"),
    settings = buildSettings ++ Seq(libraryDependencies += casbah_core)
  ) dependsOn (salatUtil)

}

object BuildSettings {

  val buildOrganization = "com.novus"
  val buildVersion = "0.0.8-SNAPSHOT"
  val buildScalaVersion = "2.8.1"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
    parallelExecution in Test := true,
    testFrameworks += TestFrameworks.Specs2,
    scalacOptions ++= Seq("-deprecation", "-unchecked")
  )
}

object Dependencies {
  val specs2 = "org.specs2" %% "specs2" % "1.4" % "test" intransitive ()
  val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test->default"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.1"
  val mongoJava = "org.mongodb" % "mongo-java-driver" % "2.5.3"
  val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.1.5.0"
  val commons_pool = "commons-pool" % "commons-pool" % "1.5.5"
}

object Repos {
  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val novusRepo = "Novus Release Repository" at "http://repo.novus.com/releases/"
  val novusSnapsRepo = "Novus Snapshots Repository" at "http://repo.novus.com/snapshots/"
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

