ThisBuild / tlBaseVersion := "0.2"
ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("bpholt", "Brian Holt"),
)
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / tlCiReleaseBranches := Seq("main")

val Scala213 = "2.13.10"
val Scala212 = "2.12.17"
val Scala3 = "3.2.2"
ThisBuild / crossScalaVersions := Seq(Scala213, Scala212, Scala3)
ThisBuild / scalaVersion := Scala213 // the default Scala
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / githubWorkflowScalaVersions := Seq("2.13", "2.12", "3")
ThisBuild / tlJdkRelease := Some(8)

ThisBuild / mergifyStewardConfig ~= {
  _.map(_.copy(mergeMinors = true, author = "typelevel-steward[bot]", action = MergifyAction.Merge(method = Option("squash"))))
}
ThisBuild / mergifySuccessConditions += MergifyCondition.Custom("#approved-reviews-by>=1")
ThisBuild / mergifyPrRules += MergifyPrRule(
  "assign scala-steward's PRs for review",
  List(MergifyCondition.Custom("author=typelevel-steward[bot]")),
  List(
    MergifyAction.RequestReviews.fromUsers("bpholt"),
  )
)

lazy val root = tlCrossRootProject.aggregate(`log4cats-natchez-backend`)

lazy val `log4cats-natchez-backend` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .settings(
    name := "log4cats-natchez",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "log4cats-core" % "2.5.0",
      "org.tpolecat" %%% "natchez-core" % "0.3.1",
    ),
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %%% "natchez-opentelemetry" % "0.3.1" % Test,
      "io.opentelemetry" % "opentelemetry-api" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-context" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-exporter-logging" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-sdk" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-sdk-common" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-sdk-trace" % "1.23.1" % Test,
      "io.opentelemetry" % "opentelemetry-semconv" % "1.21.0-alpha" % Test,
    )
  )
