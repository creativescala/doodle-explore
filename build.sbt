enablePlugins(ScalaJSBundlerPlugin)

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

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "doodle-explore-2",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.7.0",
      "org.typelevel" %%% "cats-effect" % "3.3.12",
      "co.fs2" %% "fs2-core" % "3.2.8",
      "org.creativescala" %% "doodle" % "0.10.1",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
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
      "org.creativescala" %% "doodle" % "0.10.1",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
    )
  )
  .dependsOn(core)

lazy val slinky = crossProject(JSPlatform).in(file("slinky"))

lazy val js = crossProject(JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("js"))
  .settings(
    name := "doodle-explore-2",
    scalaJSUseMainModuleInitializer := true,
    addCommandAlias("dev", ";fastOptJS::startWebpackDevServer;~fastOptJS"),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.7.0",
      "org.typelevel" %%% "cats-effect" % "3.3.12",

      /* "me.shadaj" %%% "slinky-core" % "0.7.0", */
      "co.fs2" %% "fs2-core" % "3.2.8",
      "org.creativescala" %% "doodle" % "0.10.1",
      /* "org.creativescala" %%% "doodle-svg" % "0.9.23", */
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
    ),
    scalacOptions += "-Ymacro-annotations",
    webpack / version := "4.44.2",
    startWebpackDevServer / version := "3.11.2",
    webpackResources := baseDirectory.value / "webpack" * "*",
    fastOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
    ),
    fullOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-opt.config.js"
    ),
    Test / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-core.config.js"
    ),
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"),
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
    Test / requireJsDomEnv := true,
    addCommandAlias("dev", ";fastOptJS::startWebpackDevServer;~fastOptJS"),
    addCommandAlias("build", "fullOptJS::webpack")
  )
  .dependsOn(core)
  .dependsOn(slinky)

lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
