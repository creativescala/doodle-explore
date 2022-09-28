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

package doodle.explore.laminar

import doodle.explore._
import doodle.explore.generic.ColorComponent
import doodle.explore.generic.IntComponent
import doodle.svg.Algebra
import doodle.svg.Canvas
import doodle.svg.Drawing
import doodle.svg.Frame

object Explore {
  // extends ExploreIntConstructor(IntComponent.exploreInt)
  // with ExploreColorConstructor(ColorComponent.exploreColor) {
  type Component[A] = Component.Component[A]

  // implicit val exploreInt: ExploreInt[Component, IntComponent] =
  //   doodle.explore.generic.IntComponent.exploreInt
  // implicit val exploreColor: ExploreColor[Component, ColorComponent] =
  //   doodle.explore.generic.ColorComponent.exploreColor
  implicit val layout: Layout[Component] =
    doodle.explore.generic.Layout.layout

  implicit val explorer: Explorer[Component, Algebra, Drawing, Frame, Canvas] =
    Component.laminarExplorer

  def int(label: String): IntComponent =
    IntComponent.exploreInt.int(label)

  def color(label: String): ColorComponent =
    ColorComponent.exploreColor.color(label)
}
