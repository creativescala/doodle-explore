/*
 * Copyright 2022 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doodle.explore

import cats.effect.{IO, IOApp}

import concurrent.duration.DurationInt
import cats.effect.IO.asyncForIO
import cats.effect.IO

import concurrent.duration.DurationInt

import doodle.core._
import doodle.image._
import doodle.image.syntax._
import doodle.image.syntax.all._
import doodle.image.syntax.core._
import doodle.java2d._

import doodle.java2d.effect.Center._
import doodle.java2d.effect.Redraw._
import doodle.java2d.effect.Size._

import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.interact.syntax._

import doodle.explore.java2d._
import doodle.explore._
import doodle.explore.LayoutOps._
import doodle.explore.IntComponentOps._
import doodle.explore.ColorComponentOps._
import doodle.explore.ChoiceOps._

import fs2.{Stream, Pure}

object Main {
  def simulate(angle: Angle, speed: Double, t: Double) = {
    val translation = Point(
      scala.math.cos(angle.toRadians) * speed * t,
      scala.math.sin(angle.toRadians) * speed * t
    )

    val gravity = 1.0
    val gravityVector = Vec(0.5 * gravity * t * t, -90.degrees)
    val finalPos = translation + gravityVector

    finalPos
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

  def explorer(using
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

  def main(args: Array[String]) = {
    val frame = Frame(
      FixedSize(1200.0, 1200.0),
      "Explore",
      AtOrigin,
      Some(Color.white),
      ClearToBackground
    )

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
          gravitySim(state, dt / 100.0, g / 10.0).copy(sunColor = newSunColor.value)
        }
    }

    def render(state: GravityState) = {
      val planet = Image.circle(5.0).fillColor(Color.black).at(state.pos)
      val sun = Image.circle(20.0).fillColor(state.sunColor).strokeWidth(0.0)

      planet on sun
    }

    explorer.exploreWithState(initial, update)(
      frame,
      s => Image.compile(render(s))
    )
  }
}
