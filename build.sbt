// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / organization := "org.creativescala"
ThisBuild / organizationName := "Creative Scala"
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("noelwelsh", "Noel Welsh")
)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := false

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala312 = "3.1.2"
ThisBuild / crossScalaVersions := Seq(Scala312 /*, "2.13.8"*/ )
ThisBuild / scalaVersion := Scala312 // the default Scala

lazy val root = tlCrossRootProject.aggregate(core, java2d, laminar)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "doodle-explore-2",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.7.0",
      "org.typelevel" %%% "cats-effect" % "3.3.12",
      "co.fs2" %% "fs2-core" % "3.2.8",
      "org.creativescala" %% "doodle" % "0.11.1",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test,
    )
  )

lazy val java2d = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("java2d"))
  .settings(
    name := "doodle-explore-2",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.7.0",
      "org.typelevel" %%% "cats-effect" % "3.3.12",
      "co.fs2" %% "fs2-core" % "3.2.8",
      "org.creativescala" %% "doodle" % "0.11.1",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
    )
  )
  .dependsOn(core)

lazy val laminar = crossProject(JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("laminar"))
  .settings(
    name := "doodle-explore-2",
    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass := Some("doodle.explore.laminar.Main"),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.7.0",
      "org.typelevel" %%% "cats-effect" % "3.3.12",
      "co.fs2" %%% "fs2-core" % "3.2.8",
      "org.creativescala" %%% "doodle" % "0.11.1",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test,
      "com.raquo" %%% "laminar" % "0.14.2",
      "svg" %%% "doodle-svg" % "0.1.0-SNAPSHOT",
    )
  )
  .dependsOn(core)

/* lazy val buildLaminarExample = taskKey[String]("Builds a doodle-explore laminar example") */
/* import complete.DefaultParsers._ */
/* buildLaminarExample := { */
/*     val args = spaceDelimited("<arg>").parsed */
/*     laminar.js / Compile / mainClass := Some(sys.env("MainClass")) */
/*     (laminar.js / Compile / fullOptJS).value */
/*     "test" */
/* } */

/* addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) */
lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
