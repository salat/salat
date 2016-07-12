/*
 * Copyright (c) 2010 - May 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) June 2015 - 2016 Rose Toomey and contributors where noted (http://github.com/salat/salat)
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */

import sbt._
import Keys._

object SalatBuild extends Build {

  import Dependencies._
  import BuildSettings._

  val testDeps = Seq(specs2, logbackCore, logbackClassic, casbah_specs, casbah_commons)
  val utilDeps = Seq(slf4jApi) ++ testDeps
  val coreDeps = Seq(casbah, json4sNative, commonsLang) ++ testDeps

  lazy val salat = Project(
    id = "salat",
    base = file("."),
    settings = buildSettings ++ Seq(
      publishArtifact in (Compile, packageBin) := false,
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in (Compile, packageSrc) := false
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

  val buildOrganization = "com.github.salat"
  val buildVersion = "1.9.11-SNAPSHOT"
  val buildScalaVersion = "2.11.2"

  val buildSettings = Defaults.coreDefaultSettings ++ Scalariform.settings ++ Publish.settings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
    parallelExecution in Test := false,
    testFrameworks += TestFrameworks.Specs2,
    resolvers ++= Seq(typeSafeRepo, typeSafeSnapsRepo, oss, ossSnaps),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"), 
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:_"),
    crossScalaVersions ++= Seq("2.10.4")
  )
}

object Scalariform {

import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

  val settings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := FormattingPreferences().
      setPreference(AlignArguments, true).
      setPreference(AlignParameters, true).
      setPreference(AlignSingleLineCaseStatements, true).
      setPreference(CompactControlReadability, true).
      setPreference(DoubleIndentClassDeclaration, true).
      setPreference(FormatXml, true).
      setPreference(IndentLocalDefs, true).
      setPreference(PreserveSpaceBeforeArguments, true). // otherwise scalatest DSL gets mangled
      setPreference(SpacesAroundMultiImports, false) // this agrees with IntelliJ defaults
  )
}

object Publish {

  lazy val settings = xerial.sbt.Sonatype.sonatypeSettings ++ Seq(
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
    homepage := Some(url("https://github.com/salat/salat")),
    pomExtra := (
      <scm>
        <url>git://github.com/salat/salat.git</url>
        <connection>scm:git://github.com/salat/salat.git</connection>
      </scm>
      <developers>
        <developer>
          <id>rktoomey</id>
          <name>Rose Toomey</name>
          <url>http://github.com/rktoomey</url>
        </developer>
        <developer>
          <id>noahlz</id>
          <name>Noah Zucker</name>
          <url>http://github.com/noahlz</url>
        </developer>
      </developers>)
  )
}

object Dependencies {

  private val LogbackVersion = "1.0.9"
  private val CasbahVersion = "2.7.1"

  val specs2 = "org.specs2" %% "specs2" % "2.3.11" % "test"
  val commonsLang = "commons-lang" % "commons-lang" % "2.6" % "test"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.2"
  val logbackCore = "ch.qos.logback" % "logback-core" % LogbackVersion % "test"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % LogbackVersion % "test"
  val casbah = "org.mongodb" %% "casbah-core" % CasbahVersion
  val casbah_commons = "org.mongodb" %% "casbah-commons" % CasbahVersion % "test"
  val casbah_specs = "org.mongodb" %% "casbah-commons" % CasbahVersion % "test" classifier "tests"
  val json4sNative = "org.json4s" %% "json4s-native" % "3.2.9"
}

object Repos {
  val typeSafeRepo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
  val typeSafeSnapsRepo = "Typesafe Snaps Repo" at "http://repo.typesafe.com/typesafe/snapshots/"
  val oss = "OSS Sonatype" at "http://oss.sonatype.org/content/repositories/releases/"
  val ossSnaps = "OSS Sonatype Snaps" at "http://oss.sonatype.org/content/repositories/snapshots/"
}
