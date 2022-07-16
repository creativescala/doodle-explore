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

lazy val root = tlCrossRootProject.aggregate(core, js)

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
      "org.creativescala" %% "doodle" % "0.11.1",
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
    addCommandAlias(
      "dev",
      ";jsJS/Compile/fastOptJS/startWebpackDevServer;~jsJS/fastOptJS"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.7.0",
      "org.typelevel" %%% "cats-effect" % "3.3.12",
      "me.shadaj" %%% "slinky-core" % "0.7.2",
      "me.shadaj" %%% "slinky-web" % "0.7.2",
      "me.shadaj" %%% "slinky-hot" % "0.7.2",
      "me.shadaj" %%% "slinky-scalajsreact-interop" % "0.7.2",
      "co.fs2" %% "fs2-core" % "3.2.8",
      "org.creativescala" %% "doodle" % "0.11.1",
      /* "org.creativescala" %%% "doodle-svg" % "0.9.23", */
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
    ),
    Compile / npmDependencies += "react" -> "16.13.1",
    Compile / npmDependencies += "react-dom" -> "16.13.1",
    Compile / npmDependencies += "react-proxy" -> "1.1.8",
    Compile / npmDevDependencies += "file-loader" -> "6.2.0",
    Compile / npmDevDependencies += "style-loader" -> "2.0.0",
    Compile / npmDevDependencies += "css-loader" -> "5.2.6",
    Compile / npmDevDependencies += "html-webpack-plugin" -> "4.5.1",
    Compile / npmDevDependencies += "copy-webpack-plugin" -> "6.4.0",
    Compile / npmDevDependencies += "webpack-merge" -> "5.8.0",
    fastOptJS / webpack / version := "4.44.2",
    fastOptJS / startWebpackDevServer / version := "3.11.2",
    /* fastOptJS / webpackResources := baseDirectory.value / "webpack" * "*", */
    /* fastOptJS / webpackConfigFile := Some( */
    /*   baseDirectory.value / "webpack" / "webpack-fastopt.config.js" */
    /* ), */
    /* fullOptJS / webpackConfigFile := Some( */
    /*   baseDirectory.value / "webpack" / "webpack-opt.config.js" */
    /* ), */
    /* Test / webpackConfigFile := Some( */
    /*   baseDirectory.value / "webpack" / "webpack-core.config.js" */
    /* ), */
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"),
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
    Compile / mainClass := Some("doodle.explore.js.Main"),
    Test / requireJsDomEnv := true,
    addCommandAlias("build", "fullOptJS::webpack")
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin, ScalaJSPlugin))
  .dependsOn(core)
/* .dependsOn(slinky) */

/* addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) */
lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
