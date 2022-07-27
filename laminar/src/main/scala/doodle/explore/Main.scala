package doodle.explore.laminar

import cats.effect.{IO, IOApp}
import cats.effect.IO.asyncForIO
import cats.effect.IO

import scala.concurrent.duration.DurationInt

// import doodle.svg.effect.Center._
// import doodle.svg.effect.Redraw._
// import doodle.svg.effect.Size._

import doodle.core._
import doodle.image._
import doodle.image.syntax._
import doodle.image.syntax.all._
import doodle.image.syntax.core._

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import doodle.explore.{ExploreInt, ExploreChoice, ExploreButton}
import doodle.explore.IntComponentOps._
import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.explore.laminar.Component

import doodle.interact.effect.AnimationRenderer
import doodle.effect.Renderer
import doodle.svg.effect.Frame
import doodle.svg.effect.Size.FixedSize

import org.scalajs.dom
import org.scalajs.dom.document
import cats.effect.unsafe.implicits.global

import doodle.svg.svgAnimationRenderer
import doodle.svg.svgRenderer
import doodle.explore.Layout
import doodle.explore.LayoutOps._
import doodle.explore.ExploreColor
import doodle.image.examples.CreativeScala.sierpinski

import doodle.image.Image

object Main {
  def sierpinski(n: Int, size: Int): Image = {
    def builder(component: Image) = {
      component above(
        component beside component
      )
    }

    (1 to n).foldLeft(Image.triangle(size, size)) { case (unit, _) =>
      builder(unit)
    }
  }

  def smiley(n: Int, size: Int): Image = {
    def builder(component: Image, size: Int) = {
      val smileSize = size.toDouble * 2.5
      val smile = Image.interpolatingSpline(
        List(
          Point.Polar(smileSize, -180.degrees),
          Point.Polar(smileSize * 0.8, -160.degrees),
          Point.Polar(smileSize * 0.4, -90.degrees),
          Point.Polar(smileSize * 0.8, -20.degrees),
          Point.Polar(smileSize, 0.degrees)
        )
      )

      val outline = Image.circle(smileSize * 3)

      val outlinePos = Point(0.0, -size * 2)
      val lEyePos = Point(-smileSize / 2, 0.0) - outlinePos
      val rEyePos = Point(smileSize / 2, 0.0) - outlinePos
      val smilePos = Point(0.0, -smileSize / 1) - outlinePos

      (component
        .at(lEyePos))
        .on(component.at(rEyePos))
        .on(
          smile
            .at(smilePos)
            .on(outline)
          )
    }

    (1 to n)
      .foldLeft((Image.circle(size), size)) { case ((eye, size), _) =>
        (builder(eye, size), size * 5)
      }
        ._1
  }

  def runFractal(frame: Frame, fractalFn: (Int, Int) => Image) = {
    fractalExplorer.explore(frame, { case ((size, iterations), strokeColor) => Image.compile {
      fractalFn(iterations, size).strokeColor(strokeColor)
    }})
  }

  def fractalExplorer(using
    intGui: ExploreInt[Component],
    colorGui: ExploreColor[Component],
    layoutGui: Layout[Component]
    ) = {
      import intGui._
      import colorGui._

      int("Size").within(1 to 30).startingWith(10)
      ===
      int("Iterations").within(1 to 7).startingWith(1)
      ===
      color("Stroke Color")
  }

  case class GravityState(pos: Vec, vel: Vec, mass: Double, sunColor: Color)
  def gravitySim(state: GravityState, dt: Double, g: Double): GravityState = {
    val sunMass = 100.0
    val force = g * sunMass * state.mass / (state.pos.length * state.pos.length)

    val accelMag = force / state.mass
    val accelDir = Vec(0.0, 0.0) - state.pos
    val accel = accelDir * accelMag

    state.copy(pos = state.pos + state.vel * dt, vel = state.vel + accel * dt)
  }

  def gravityExplorer(using
      intGui: ExploreInt[Component],
      choiceGui: ExploreChoice[Component],
      buttonGui: ExploreButton[Component],
      layout: Layout[Component]
  ) = {
    import intGui._
    import choiceGui._
    import buttonGui._

    int("G").within(0 to 10).startingWith(1)
    ===
    int("DT").within(1 to 100).startingWith(16)
    ===
    int("Start Velocity").within(0 to 100).startingWith(30)
    ===
    labeledChoice(
      "Sun Color",
      Seq(
        ("Yellow" -> Color.yellow),
        ("Red" -> Color.red),
        ("Blue" -> Color.blue)
      )
    )
    ===
    button("Reset")
  }

  def runGravitySim(frame: Frame) = {
    val initial = GravityState(
      Vec(300.0, 0.degrees),
      Vec(3.0, 90.degrees),
      0.1,
      Color.yellow
    )
    val update: (
      GravityState,
      ((((Int, Int), Int), Color), Boolean)
    ) => GravityState = {
      case (state, ((((g, dt), startVel), newSunColor), reset)) =>
        if (reset) {
          initial.copy(vel = Vec(startVel / 10.0, 90.degrees))
        } else {
          gravitySim(state, dt / 100.0, g / 10.0).copy(sunColor = newSunColor)
        }
    }

    def render(state: GravityState) = {
      val planet = Image.circle(5.0).fillColor(Color.black).at(state.pos)
      val sun = Image.circle(20.0).fillColor(state.sunColor).strokeWidth(0.1)

      planet on sun
    }

    gravityExplorer.exploreWithState(initial, update)(frame, s => Image.compile(render(s)))
  }

  def main(args: Array[String]): Unit = {
    val container = dom.document.querySelector("#container")
    val frame = Frame("doodle")
    val app = div(
      span("Select a doodle: "),
      select(
        option("Sierpinski"),
        option("Smiley"),
        option("Gravity"),
        inContext { node => onChange.mapTo(node.ref.value) --> { choice =>
          dom.document.querySelector("#doodle").innerHTML = ""
          dom.document.querySelector("#explorer").innerHTML = ""
          choice match {
            case "Sierpinski" => runFractal(frame, sierpinski)
            case "Smiley" => runFractal(frame, smiley)
            case "Gravity" => runGravitySim(frame.size(800, 800))
          },
        }})
      )

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(container, app)
      runFractal(frame, sierpinski)
    }(unsafeWindowOwner)
  }
}
