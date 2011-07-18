import sbt._
import Keys._

object SalatBuild extends Build {
  
  import Dependencies._
  import BuildSettings._

  val utilDeps = Seq(specs2, slf4jSimple)
  val coreDeps = Seq(mongoJava, casbah_core, commonsLang, specs2)

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
    // TODO: ask on the specs2 mailing list why thisa dependency so consistently fails to resolve properly
//      libraryDependencies += "org.specs2" %% "specs2-scalaz-core" % "5.1-SNAPSHOT" from "http://scala-tools.org/repo-snapshots/org/specs2/specs2-scalaz-core_2.8.1/5.1-SNAPSHOT/specs2-scalaz-core_2.8.1-5.1-SNAPSHOT.jar",
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _)
    )
    Project(id = id, base = base, settings = settings)
  }

  lazy val salatCore = Project(
    id = "salat-core",
    base = file("salat-core"),
    settings = buildSettings ++ Seq(libraryDependencies ++= coreDeps)
  ) dependsOn (salatUtil)

}

object BuildSettings {

  import Repos._
  
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
    resolvers ++= Seq(scalaToolsRepo, scalaToolsSnapRepo, novusRepo, novusSnapsRepo, typeSafeRepo), 
    scalacOptions ++= Seq("-deprecation", "-unchecked")
  )
}

object Dependencies {
  val specs2 = "org.specs2" %% "specs2" % "1.5" % "test"
  val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.1"
  val mongoJava = "org.mongodb" % "mongo-java-driver" % "2.5.3"
  val casbah_core = "com.mongodb.casbah" %% "casbah-core" % "2.1.5.0"
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

