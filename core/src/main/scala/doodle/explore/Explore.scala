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

package doodle
package explore

import doodle.core._
import doodle.image._
import doodle.algebra.{Algebra, Picture}
import doodle.image.syntax.all._
import doodle.image.syntax.core._
import doodle.interact.syntax.all._

import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.interact.syntax._

import doodle.java2d.effect.Center._
import doodle.java2d.effect.Redraw._
import doodle.java2d.effect.Size._

import fs2.Stream
import fs2.Pure

import cats.effect.IO
import doodle.interact.effect.AnimationRenderer
import doodle.effect.Renderer

import cats.kernel.Eq

/** An `Explorer[A]` is the base type that describes how to render an explore
  * GUI for a given backend. The reference example is
  * [[doodle.explore.java2d.Component]]
  */
trait Explorer[
    Component[_],
    Alg[x[_]] <: Algebra[x],
    Drawing[_],
    Frame,
    Canvas
] {

  extension [A](component: Component[A]) {
    def explore(frame: Frame)(render: A => Picture[Alg, Drawing, Unit])(using
        a: AnimationRenderer[Canvas],
        r: Renderer[Alg, Drawing, Frame, Canvas]
    ): Unit

    def exploreScan[B](
        frame: Frame
    )(initial: B)(scan: (B, A) => B)(render: B => Picture[Alg, Drawing, Unit])(
        using
        a: AnimationRenderer[Canvas],
        r: Renderer[Alg, Drawing, Frame, Canvas]
    ): Unit
  }

  implicit object EqColor extends Eq[Color] {
    def eqv(a: Color, b: Color) = a ~= b
  }
}

enum LayoutDirection {
  case Vertical
  case Horizontal
}
