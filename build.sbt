import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

import sbt.Keys.libraryDependencies
import Settings._

onLoadMessage := s"Welcome to Safe Builder"

inThisBuild(
  Seq(
    organization := "com.github.pcejrowski",
    name := "safe-builder",
    scalacOptions in Compile ++= ScalacOptions,
    javacOptions in Compile ++= JavacOptions,
    scalafmtTestOnCompile := true,
    scalafmtTestOnCompile := true,
    crossScalaVersions := Seq("2.12.8")
  )
)

lazy val root = (project in file("."))
  .aggregate(coreJVM, coreJS, coreNative)
  .settings(skip in publish := true)
  .settings(ReleaseSettings:_*)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(PublishSettings:_*)
  .settings(
    moduleName := "safe-builder",
    scalaVersion := "2.12.8",
    crossScalaVersions := Seq("2.11.12", "2.12.8"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scalatest" %%% "scalatest" % "3.1.0-SNAP8" % Test
    )
  )
  .nativeSettings(
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.12"),
    nativeLinkStubs := true,
    sources in (Compile, doc) := Seq.empty, // https://github.com/scala-native/scala-native/issues/1121
  )

lazy val coreJVM    = core.jvm
lazy val coreJS     = core.js
lazy val coreNative = core.native
