import laika.ast.LengthUnit._
import laika.ast._
import laika.helium.Helium
import laika.helium.config.Favicon
import laika.helium.config.HeliumIcon
import laika.helium.config.IconLink
import laika.helium.config.ImageLink
import TypelevelGitHubPlugin._

Global / onChangedBuildSource := ReloadOnSourceChanges

// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.12" // your current series x.y

ThisBuild / organization := "org.creativescala"
ThisBuild / organizationName := "Creative Scala"
ThisBuild / organizationHomepage := Some(url("http://creativescala.org/"))
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("noelwelsh", "Noel Welsh")
)

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / tlSonatypeUseLegacyHost := true

ThisBuild / tlSitePublishBranch := Some("main")

val Scala3 = "3.2.0"
ThisBuild / crossScalaVersions := Seq(Scala3)
ThisBuild / scalaVersion := Scala3

// Dependencies used by all the sub-projects
ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %%% "cats-core" % "2.8.0",
  "org.typelevel" %%% "cats-effect" % "3.3.14",
  "co.fs2" %%% "fs2-core" % "3.3.0",
  "org.creativescala" %%% "doodle" % "0.11.2",
  "org.scalameta" %%% "munit" % "0.7.29" % Test,
  "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
)

// Run this (build) to do everything involved in building the project
commands += Command.command("build") { state =>
  "dependencyUpdates" ::
    "compile" ::
    "test" ::
    "scalafixAll" ::
    "scalafmtAll" ::
    "headerCreate" ::
    state
}

lazy val root = crossProject(JSPlatform, JVMPlatform).in(file("."))
lazy val rootJvm =
  root.jvm.dependsOn(core.jvm, java2d).aggregate(core.jvm, java2d)
lazy val rootJs =
  root.js.dependsOn(core.js, laminar).aggregate(core.js, laminar)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(name := "doodle-explore")

lazy val java2d = project
  .in(file("java2d"))
  .dependsOn(core.jvm)

lazy val laminar = project
  .in(file("laminar"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "doodle-explore",
    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass := Some("doodle.explore.laminar.Main"),
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.14.2",
      "org.creativescala" %%% "doodle-svg" % "0.11.3"
    )
  )
  .dependsOn(core.js)

/* lazy val buildLaminarExample = taskKey[String]("Builds a doodle-explore laminar example") */
/* import complete.DefaultParsers._ */
/* buildLaminarExample := { */
/*     val args = spaceDelimited("<arg>").parsed */
/*     laminar.js / Compile / mainClass := Some(sys.env("MainClass")) */
/*     (laminar.js / Compile / fullOptJS).value */
/*     "test" */
/* } */

/** Example that is used in documentation */
lazy val example = project
  .in(file("example"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.14.2",
      "org.creativescala" %%% "doodle-svg" % "0.11.3"
    )
  )
  .dependsOn(laminar)

lazy val docs = project
  .in(file("site"))
  .settings(
    tlSiteRelatedProjects := Seq(
      ("Doodle", url("https://creativescala.org/doodle")),
      ("Doodle SVG", url("https://creativescala.github.io/doodle-svg")),
      ("Creative Scala", url("https://creativescala.org"))
    ),
    laikaExtensions += DoodleDirectives,
    tlSiteHeliumConfig := {
      Helium.defaults.site
        .metadata(
          title = Some("Doodle Explore"),
          authors = developers.value.map(_.name),
          language = Some("en"),
          version = Some(version.value.toString)
        )
        .site
        .layout(
          contentWidth = px(860),
          navigationWidth = px(275),
          topBarHeight = px(50),
          defaultBlockSpacing = px(10),
          defaultLineHeight = 1.5,
          anchorPlacement = laika.helium.config.AnchorPlacement.Right
        )
        .site
        .darkMode
        .disabled
        .site
        .favIcons(
          Favicon.external(
            "https://typelevel.org/img/favicon.png",
            "32x32",
            "image/png"
          )
        )
        .site
        .topNavigationBar(
          homeLink = IconLink.external(
            "https://creativescala.org",
            HeliumIcon.home
          ),
          navLinks = tlSiteApiUrl.value.toList.map { url =>
            IconLink.external(
              url.toString,
              HeliumIcon.api,
              options = Styles("svg-link")
            )
          } ++ List(
            IconLink.external(
              scmInfo.value
                .fold("https://github.com/creativescala")(_.browseUrl.toString),
              HeliumIcon.github,
              options = Styles("svg-link")
            )
            // IconLink.external("https://discord.gg/XF3CXcMzqD", HeliumIcon.chat),
            // IconLink.external("https://twitter.com/typelevel", HeliumIcon.twitter)
          )
        )
    },
    Laika / sourceDirectories +=
      (example / Compile / fastOptJS / artifactPath).value
        .getParentFile() / s"${(example / moduleName).value}-fastopt",
    tlSite := Def
      .sequential(
        (example / Compile / fastOptJS),
        mdoc.toTask(""),
        laikaSite
      )
      .value
  )
  .enablePlugins(TypelevelSitePlugin)
