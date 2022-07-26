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

lazy val root = tlCrossRootProject.aggregate(core, java2d, js, doodle_svg)

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
    Compile / npmDependencies ++= Seq(
      "react" -> "16.13.1",
      "react-dom" -> "16.13.1",
      "react-proxy" -> "1.1.8",
      "file-loader" -> "6.2.0",
      "style-loader" -> "2.0.0",
      "css-loader" -> "5.2.6",
      "html-webpack-plugin" -> "4.5.1",
      "copy-webpack-plugin" -> "6.4.0",
      "webpack-merge" -> "5.8.0"
    ),
    Compile / npmDevDependencies ++= Seq(
      "file-loader" -> "6.0.0",
      "style-loader" -> "1.2.1",
      "css-loader" -> "3.5.3",
      "html-webpack-plugin" -> "4.3.0",
      "copy-webpack-plugin" -> "5.1.1",
      "webpack-merge" -> "4.2.2",
      "postcss-loader" -> "4.1.0",
      "postcss" -> "8.2.6",
      "tailwindcss" -> "2.0.1",
      "autoprefixer" -> "10.0.2",
      "react-icons" -> "4.1.0"
    ),
    fastOptJS / webpack / version := "4.44.2",
    fastOptJS / startWebpackDevServer / version := "3.11.2",
    webpackResources := baseDirectory.value / "webpack" * "*",
    fastOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
    ),
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"),
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
    fullOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-opt.config.js"
    ),
    Test / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-core.config.js"
    ),
    Compile / mainClass := Some("doodle.explore.js.Main"),
    Test / requireJsDomEnv := true,
    addCommandAlias("build", "fullOptJS::webpack")
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin, ScalaJSPlugin))
  .dependsOn(core)

lazy val doodle_svg = crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("doodle-svg"))
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
      "org.creativescala" %% "doodle" % "0.11.1",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test,
      "com.raquo" %%% "laminar" % "0.13.1"
    )
  )
  .dependsOn(core, doodle_svg)

/* addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) */
lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
