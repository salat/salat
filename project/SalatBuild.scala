/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-build
 * Class:         SalatBuild.scala
 * Last modified: 2013-01-07 22:28:16 EST
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */

import sbt._
import Keys._

object SalatBuild extends Build {

  import Dependencies._
  import BuildSettings._

  val testDeps = Seq(specs2, logbackCore, logbackClassic)
  val utilDeps = Seq(slf4jApi) ++ testDeps
  val coreDeps = Seq(casbah, json4sNative, commonsLang) ++ testDeps

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
  val buildVersion = "1.9.2-SPECIAL"
  val buildScalaVersion = "2.10.0"

  val buildSettings = Defaults.defaultSettings ++ Format.settings ++ Publish.settings ++ Ls.settings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
    parallelExecution in Test := false,
    testFrameworks += TestFrameworks.Specs2,
    resolvers ++= Seq(typeSafeRepo, typeSafeSnapsRepo, oss, ossSnaps),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")/*,
    crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0-RC3")*/   // as usual, this actually won't cross build....
  )
}

object Format {

  import com.typesafe.sbt.SbtScalariform._

  lazy val settings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := formattingPreferences
  )

  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences().
      setPreference(AlignParameters, true).
      setPreference(AlignSingleLineCaseStatements, true).
      setPreference(CompactControlReadability, true).
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
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/novus/salat")),
    pomExtra := (
      <scm>
        <url>git://github.com/novus/salat.git</url>
        <connection>scm:git://github.com/novus/salat.git</connection>
      </scm>
      <developers>
        <developer>
          <id>rktoomey</id>
          <name>Rose Toomey</name>
          <url>http://github.com/rktoomey</url>
        </developer>
      </developers>)
  )
}

object Dependencies {

  private val LogbackVersion = "1.0.9"

  val specs2 = "org.specs2" %% "specs2" % "1.13" % "test"
  val commonsLang = "commons-lang" % "commons-lang" % "2.6" % "test"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.2"
  val logbackCore = "ch.qos.logback" % "logback-core" % LogbackVersion % "test"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % LogbackVersion % "test"
  val casbah = "org.mongodb" %% "casbah-core" % "2.5.0"
  val json4sNative = "org.json4s" %% "json4s-native" % "3.1.0"
}

object Repos {
  val typeSafeRepo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
  val typeSafeSnapsRepo = "Typesafe Snaps Repo" at "http://repo.typesafe.com/typesafe/snapshots/"
  val oss = "OSS Sonatype" at "http://oss.sonatype.org/content/repositories/releases/"
  val ossSnaps = "OSS Sonatype Snaps" at "http://oss.sonatype.org/content/repositories/snapshots/"
}

// Shell prompt which show the current project, git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Ls {

  import _root_.ls.Plugin.LsKeys._

  lazy val settings = _root_.ls.Plugin.lsSettings ++ Seq(
    (description in lsync) := "A simple serialization library for case classes.",
    licenses in lsync <<= licenses,
    (tags in lsync) := Seq("mongo", "casbah", "json", "serialization", "object document mapping", "ODM", "mapper", "play", "salat"),
    (docsUrl in lsync) := Some(new URL("https://github.com/novus/salat/wiki"))
  )
}
