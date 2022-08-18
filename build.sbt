// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.1" // your current series x.y

ThisBuild / organization := "org.creativescala"
ThisBuild / organizationName := "Creative Scala"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("noelwelsh", "Noel Welsh")
)

ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / tlSitePublishBranch := Some("main")

val Scala312 = "3.1.2"
ThisBuild / crossScalaVersions := Seq(Scala312)
ThisBuild / scalaVersion := Scala312

// Dependencies used by all the sub-projects
ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %%% "cats-core" % "2.7.0",
  "org.typelevel" %%% "cats-effect" % "3.3.12",
  "co.fs2" %% "fs2-core" % "3.2.8",
  "org.creativescala" %% "doodle" % "0.11.2",
  "org.scalameta" %%% "munit" % "0.7.29" % Test,
  "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
)

lazy val root = tlCrossRootProject.aggregate(core, java2d, laminar)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(name := "doodle-explore")

lazy val java2d = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("java2d"))
  .settings(name := "doodle-explore")
  .dependsOn(core)

lazy val laminar = crossProject(JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("laminar"))
  .settings(
    name := "doodle-explore",
    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass := Some("doodle.explore.laminar.Main"),
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.14.2",
      "org.creativescala" %%% "doodle-svg" % "0.11.2"
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
lazy val docs = project
  .in(file("site"))
  .settings(
    tlSiteRelatedProjects := Seq(
      ("Doodle", url("https://creativescala.org/doodle")),
      ("Doodle SVG", url("https://creativescala.github.io/doodle-svg")),
      ("Creative Scala", url("https://creativescala.org"))
    )
  )
  .enablePlugins(TypelevelSitePlugin)
