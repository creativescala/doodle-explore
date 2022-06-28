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

import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.interact.syntax._

import doodle.explore.java2d._
import doodle.explore._

import fs2.Stream

class TestSuite extends CatsEffectSuite {
  test("Infinite Smiley") {
    def smiley(n: Int, size: Int): Image = {
      def builder(component: Image, size: Int) = {
        val smileSize = size.toDouble * 2.5
        val smile = Image.interpolatingSpline(List(
          Point.Polar(smileSize, -180.degrees), 
          Point.Polar(smileSize * 0.8, -160.degrees), 
          Point.Polar(smileSize * 0.4, -90.degrees), 
          Point.Polar(smileSize * 0.8, -20.degrees), 
          Point.Polar(smileSize, 0.degrees)
        ))

        val outline = Image.circle(smileSize * 3)

        val lEyePos = Point(-smileSize / 2, 0.0)
        val rEyePos = Point(smileSize / 2, 0.0)
        val smilePos = Point(0.0, -smileSize / 1)
        val outlinePos = Point(0.0, -size * 2)

        (component.at(lEyePos))
          .on(component.at(rEyePos))
          .on(smile.at(smilePos)
          .on(outline.at(outlinePos)))
      }

      (1 to n).foldLeft((Image.circle(size), size)) { case ((unit, size), _) => 
        (builder(unit, size), size * 5)
      }._1
    }

    def explorer(implicit 
      intGui: ExploreInt[Component, IntSlider], 
      colorGui: ExploreColor[Component],
      layout: Layout[Component]
    ) = {
      import intGui._
      import colorGui._
      // import layout._

      (int("Base Size").within(1, 60).startingWith(10))
        .above(int("Iterations").within(1, 5).startingWith(1))
        .above(int("Stroke Width").within(1, 20).startingWith(2))
        .above(color("Background").withDefault(Color.white))
        .above(color("Foreground").withDefault(Color.black))
        .above(
          (int("X Offset").within(-1000, 1000))
            .beside(int("Y Offset").within(-1000, 1000))
        )
    }


    val frame = Frame(FixedSize(1200.0, 1200.0), "Explore", CenteredOnPicture, Some(Color.white), ClearToBackground)

    explorer.explore(frame, { 
      case ((((((size, iterations), stroke), background), foreground), (xOffset, yOffset))) =>
        val smile = smiley(iterations, size).strokeColor(foreground).strokeWidth(stroke)
        val backgroundCircle = Image.circle(2400).fillColor(background)

        Image.compile {
          (smile at Point(xOffset, yOffset)) on backgroundCircle
        }
    })
  }
}
