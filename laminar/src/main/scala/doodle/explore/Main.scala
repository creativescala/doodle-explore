package doodle.explore.laminar

import doodle.core._
import doodle.image._
import doodle.image.syntax.core._

import doodle.explore.{
  ExploreInt,
  ExploreChoice,
  ExploreBoolean,
  ExploreColor,
  Layout
}
import doodle.explore.syntax.all._
import doodle.explore.Choice

import doodle.svg.effect.Frame
import doodle.svg.svgAnimationRenderer
import doodle.svg.svgRenderer

import org.scalajs.dom
import org.scalajs.dom.document

import com.raquo.laminar.api.L.{*, given}
import doodle.image.Image

object Fractals {
  def sierpinski(n: Int, size: Int): Image = {
    def builder(component: Image) = {
      component above (
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
    fractalExplorer.explore(
      frame,
      { case ((size, iterations), strokeColor) =>
        Image.compile {
          fractalFn(iterations, size).strokeColor(strokeColor)
        }
      }
    )
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
}

object Gravity {
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
      booleanGui: ExploreBoolean[Component],
      layout: Layout[Component]
  ) = {
    import intGui._
    import choiceGui._
    import booleanGui._

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
        ((((Int, Int), Int), Choice[Color]), Boolean)
    ) => GravityState = {
      case (state, ((((g, dt), startVel), newSunColor), reset)) =>
        if (reset) {
          initial.copy(vel = Vec(startVel / 10.0, 90.degrees))
        } else {
          gravitySim(state, dt / 100.0, g / 10.0)
            .copy(sunColor = newSunColor.value)
        }
    }

    def render(state: GravityState) = {
      val planet = Image.circle(5.0).fillColor(Color.black).at(state.pos)
      val sun = Image.circle(20.0).fillColor(state.sunColor).strokeWidth(0.1)

      planet on sun
    }

    gravityExplorer.exploreWithState(initial, update)(
      frame,
      s => Image.compile(render(s))
    )
  }
}

object Tree {
  def treeExplorer(using
      intGui: ExploreInt[Component],
      layout: Layout[Component]
  ) = {
    import intGui._

    int("Depth").within(1 to 12).startingWith(3)
    ===
    int("Length").within(1 to 2500).startingWith(500)
  }

  def runTree(frame: Frame) = {
    treeExplorer.explore(
      frame,
      { case (depth, length) =>
        Image.compile {
          doodle.image.examples.Tree.branch(depth, 0.degrees, length / 10.0)
        }
      }
    )
  }
}

object Sine {
  def sineExplorer(using
      intGui: ExploreInt[Component],
      colorGui: ExploreColor[Component],
      layout: Layout[Component]
  ) = {
    import intGui._
    import colorGui._

    int("Width").within(0 to 2000).startingWith(1000)
    ===
    int("Amplitude").within(0 to 2500).startingWith(500)
    ===
    int("Period").within(1 to 1000).startingWith(600)
    ===
    color("Stroke Color")
  }

  def runSine(frame: Frame) = {
    sineExplorer.explore(
      frame,
      { case (((width, amplitude), period), color) =>
        Image.compile {
          val curve =
            doodle.image.examples.Sine.sine(width, amplitude / 10.0, period)

          doodle.image.examples.Sine.styledSine(curve).strokeColor(color)
        }
      }
    )
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val container = dom.document.querySelector("#container")
    val frame = Frame("doodle")
    val app = div(
      span("Select a doodle: "),
      select(
        option("Sierpinski"),
        option("Smiley"),
        option("Gravity"),
        option("Tree"),
        option("Sine"),
        inContext { node =>
          onChange.mapTo(node.ref.value) --> { choice =>
            dom.document.querySelector("#doodle").innerHTML = ""
            dom.document.querySelector("#explorer").innerHTML = ""
            choice match {
              case "Sierpinski" =>
                Fractals.runFractal(frame, Fractals.sierpinski)
              case "Smiley"  => Fractals.runFractal(frame, Fractals.smiley)
              case "Gravity" => Gravity.runGravitySim(frame.size(800, 800))
              case "Tree"    => Tree.runTree(frame)
              case "Sine"    => Sine.runSine(frame)
            },
          }
        }
      )
    )

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(container, app)
      Fractals.runFractal(frame, Fractals.sierpinski)
    }(unsafeWindowOwner)
  }
}
