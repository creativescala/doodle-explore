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

import doodle.core._
import doodle.image._
import doodle.image.syntax.all._
import doodle.java2d._

import doodle.java2d.effect.Center._
import doodle.java2d.effect.Redraw._
import doodle.java2d.effect.Size._

import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.interact.syntax._

import doodle.explore.java2d._
import doodle.explore._
import doodle.explore.syntax.all._
import doodle.explore.ChoiceConversions._

import fs2.{Stream, Pure}

object Main extends App {
  def explorer(using
    intGui: ExploreInt[Component],
    colorGui: ExploreColor[Component],
    layoutGui: Layout[Component],
    ) = {
      import intGui._
      import colorGui._

      int("Size").within(50 to 750) 
        .beside(int("Iterations").within(1 to 6).startingWith(2))
        .above(color("Stroke Color"))
  }

  val frame = Frame(
    FixedSize(1200.0, 1200.0),
    "Explore",
    AtOrigin,
    Some(Color.white),
    ClearToBackground,
    )

  explorer.explore(frame, { case ((size, iterations), color) =>
    Image.compile {
      doodle.image.examples.Sierpinski.sierpinski(iterations, size).strokeColor(color)
    }
  })
}
