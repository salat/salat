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

  lazy val salat = Project(
    id = "salat",
    base = file("."),
    settings = buildSettings ++ Seq(
      publishArtifact in (Compile, packageBin) := false,
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in (Compile, packageSrc) := false
    ),
    aggregate = Seq(util, core, salat_json4s, salat_casbah, salat_reactive_mongo)
  ).configs( IntegrationTest )
   .settings( Defaults.itSettings : _*) dependsOn(util, core, salat_json4s, salat_casbah, salat_reactive_mongo)

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

  // and here begin the first of - I hope - many useful modules!

  lazy val salat_json4s = Project(
    id = "salat-json4s",
    base = file("salat-json4s"),
    settings = buildSettings ++ Seq(libraryDependencies ++= json4sDeps)
  ) dependsOn (core)

  // TODO: play json

  lazy val salat_casbah = Project(
    id = "salat-casbah",
    base = file("salat-casbah"),
    settings = buildSettings ++ Seq(libraryDependencies ++= casbahDeps)
  ) dependsOn (core)

  lazy val salat_reactive_mongo = Project(
    id = "salat-reactivemongo",
    base = file("salat-reactivemongo"),
    settings = buildSettings ++ Seq(libraryDependencies ++= reactiveMongoDeps)
  ) dependsOn (core)

}

object BuildSettings {

  import Repos._

  val buildOrganization = "com.novus"
  val buildVersion = "1.9.3-SNAPSHOT"
  val buildScalaVersion = "2.10.2"

  val buildSettings = Defaults.defaultSettings ++ Format.settings ++ Publish.settings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
    parallelExecution in Test := false,
    testFrameworks += TestFrameworks.Specs2,
    resolvers ++= Seq(typeSafeRepo, typeSafeSnapsRepo, oss, ossSnaps),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"), 
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:_")
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
  private val CasbahVersion = "2.6.2"

  private val specs2 = "org.specs2" %% "specs2" % "2.2-SNAPSHOT" % "it,test"
  private val commonsLang = "commons-lang" % "commons-lang" % "2.6" % "it,test"
  private val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.2"
  private val logbackCore = "ch.qos.logback" % "logback-core" % LogbackVersion % "it,test"
  private val logbackClassic = "ch.qos.logback" % "logback-classic" % LogbackVersion % "it,test"
  private val casbah = "org.mongodb" %% "casbah-core" % CasbahVersion
  private val casbah_commons = "org.mongodb" %% "casbah-commons" % CasbahVersion % "it,test"
  private val casbah_specs = "org.mongodb" %% "casbah-commons" % CasbahVersion % "it,test" classifier "test"
  private val json4sNative = "org.json4s" %% "json4s-native" % "3.1.0"
  private val reactiveMongo = "org.reactivemongo" %% "reactivemongo" % "0.9"

  private val baseTestDeps = Seq(specs2, logbackCore, logbackClassic)
  val utilDeps = baseTestDeps ++ Seq(slf4jApi)
  val json4sDeps = Seq(json4sNative)
  val coreDeps = Seq(commonsLang)
  val casbahDeps = Seq(casbah, casbah_specs, casbah_commons)
  val reactiveMongoDeps = Seq(reactiveMongo)

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

  def currBranch() = ("git rev-parse --abbrev-ref HEAD" lines_! devnull).headOption.getOrElse("???")

  private def parseToBool(cmd: String) = (cmd lines_! devnull).headOption.exists(_ == "true")

  private def successfulExit(cmd: String) = cmd ! devnull == 0

  private def unsuccessfulExit(cmd: String) = !successfulExit(cmd)

  def showDirtyState() = {
    // transposed from git-completion.bash
    val insideGitDir = parseToBool("git rev-parse --is-inside-git-dir")
    val insideWorkTree = !insideGitDir && parseToBool("git rev-parse --is-inside-work-tree")
    val showDirtyState = insideWorkTree && unsuccessfulExit("git config --bool bash.showDirtyState") // for some reason, this exits with 1 but that's what we want
    if (showDirtyState) {
      // modified
      val w = if (unsuccessfulExit("git diff --no-ext-diff --quiet --exit-code")) "*" else ""
      // added
      val i = if (successfulExit("git rev-parse --quiet --verify HEAD") && unsuccessfulExit("git diff-index --cached --quiet HEAD --")) "+" else ""
      // stashed
      val s = if (successfulExit("git rev-parse --verify refs/stash")) "$" else ""
      // untracked
      val u = if (("git ls-files --others --exclude-standard" lines_! devnull).nonEmpty) "%" else ""
      "%s%s%s%s".format(w, i, s, u)
    }
    else ""
  }

  private def showUpstream() = {
    val counts = ("git rev-list --count --left-right @{upstream}...HEAD" lines_! devnull).headOption.getOrElse("")
    if (counts.isEmpty) "" // no upstream
    else if (counts == "0\t0") "=" // equal to upstream
    else if (counts.startsWith("0")) ">" // ahead of upstream
    else if (counts.endsWith("\t0")) "<" // behind upstream
    else "<>" // oh crumbs
  }

  val buildShellPrompt = (state: State) => {
    // thanks to https://groups.google.com/forum/#!searchin/simple-build-tool/How$20to$20color$20ShellPrompt/simple-build-tool/H7HcaYiv8FM/lMa2aVgCa3gJ
    // and http://mediadoneright.com/content/ultimate-git-ps1-bash-prompt
    val p = Project.extract(state)
    val projectNameOrId = p.getOpt(sbt.Keys.name).getOrElse(p.currentProject.id)
    val projectVersion = p.getOpt(sbt.Keys.version).getOrElse("")

    import scala.Console.RESET
    val YELLOW_I = "\033[0;92m"
    val GREEN_I = "\033[0;92m"
    val RED_I = "\033[0;91m"
    val BLACK_I = "\033[0;90m"
    val CYAN_I = "\033[0;96m"

    val projectVersionColor =
      if (("git status" lines_! devnull).filter(_.contains("nothing to commit")).nonEmpty) GREEN_I
      else RED_I

    "%s%s%s %s%s%s %s%s%s%s%s%s%s $ ".format(
      CYAN_I, projectNameOrId, RESET,
      BLACK_I, projectVersion, RESET,
      projectVersionColor, "(", currBranch(), showDirtyState(), showUpstream(), ")", RESET)
  }
}
