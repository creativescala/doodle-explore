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

import munit.CatsEffectSuite
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

import doodle.syntax.all.RendererFrameOps
import doodle.interact.syntax._

import doodle.explore.java2d._
import doodle.explore._

import fs2.Stream

class TestSuite extends CatsEffectSuite {
  test("Basic GUI") {
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

    def explorer(implicit 
      intGui: ExploreInt[Component, IntSlider], 
      colorGui: ExploreColor[Component],
      layout: Layout[Component]
    ) = {
      import intGui._
      import colorGui._
      // import layout._

      (int("Base Size") within(0, 100) startingWith(20))
        .above(int("Iterations") within(1, 10) startingWith(2))
        .above(int("Rotation") within(-180, 180))
        .above(color("Stroke Color"))
    }

    val ui = explorer
    ui.show

    val frame = Frame(FixedSize(800.0, 800.0), "Explore", CenteredOnPicture, Some(Color.white), ClearToBackground)
    frame.canvas().flatMap { canvas =>
      val frames: Stream[IO, Picture[Unit]] =
        ui.values
          .map { case (((size, iterations), angle), color) =>
            Image.compile(sierpinski(iterations, size).rotate(angle.toDouble.degrees).strokeColor(color))
          }

      frames.animateWithCanvasToIO(canvas)
    }
  }
}
