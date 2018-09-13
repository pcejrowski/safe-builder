import sbt._
import sbt.Keys._
import bintray.BintrayKeys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

object Settings {
  val ScalacOptions = Seq(
    "-deprecation",
    "-language:_",
    "-Xfatal-warnings",
    "-unchecked",
    "-feature",
    "-Xplugin-require:macroparadise") // :+ MacroDebugging

  val MacroDebugging = "-Ymacro-debug-lite"

  val JavacOptions = Seq("-source", "1.8", "-target", "1.8")

  val PublishSettings = Seq(
    bintrayOrganization := Some("pcejrowski"),
    bintrayRepository := "maven",
    autoAPIMappings := true,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishArtifact in Test := false,
    licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
    homepage := Some(new URL("https://github.com/pcejrowski/safe-builder")),
    developers := List(
      Developer(
        id = "pcejrowski",
        name = "Paweł Cejrowski",
        email = "pcejrowski@gmail.com",
        url = url("http://github.com/pcejrowski")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/pcejrowski/safe-builder"),
        "scm:git:git://github.com/pcejrowski/safe-builder.git"
      )
    )
  )

  val ReleaseSettings = Seq(
    releaseCrossBuild := false,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      releaseStepCommandAndRemaining("+clean"),
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}
