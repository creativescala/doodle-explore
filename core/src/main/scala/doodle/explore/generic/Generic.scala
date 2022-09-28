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

package doodle.explore.generic

import doodle.core.Color
import doodle.explore.*

sealed abstract class BaseComponent[A] extends Product with Serializable

sealed abstract class Layout[A, B] extends BaseComponent[(A, B)]
final case class Above[A, B](top: BaseComponent[A], bottom: BaseComponent[B])
    extends Layout[A, B]
final case class Beside[A, B](left: BaseComponent[A], right: BaseComponent[B])
    extends Layout[A, B]
object Layout {
  implicit val layout: doodle.explore.Layout[BaseComponent] =
    new doodle.explore.Layout[BaseComponent] {
      extension [A, B](top: BaseComponent[A]) {
        def above(bottom: BaseComponent[B]): BaseComponent[(A, B)] =
          Above(top, bottom)
      }
      extension [A, B](left: BaseComponent[A]) {
        def beside(right: BaseComponent[B]): BaseComponent[(A, B)] =
          Beside(left, right)
      }
    }
}

final case class IntComponent(
    label: String,
    range: Option[(Int, Int)],
    default: Int
) extends BaseComponent[Int] {
  def within(start: Int, stop: Int): IntComponent =
    this.copy(range = Some((start, stop)), default = (stop - start) / 2)

  def withDefault(default: Int): IntComponent = {
    range match {
      case None => this.copy(default = default)
      case Some((start, stop)) =>
        if (default < start || default > stop) this
        else this.copy(default = default)
    }
  }

  def within(range: Range): IntComponent =
    if (range.isInclusive) this.within(range.start, range.end)
    else this.within(range.start, range.end - 1)
}
object IntComponent {
  implicit val exploreInt: ExploreInt[BaseComponent, IntComponent] =
    new ExploreInt[BaseComponent, IntComponent] {
      def int(label: String): IntComponent = IntComponent(label, None, 0)

      extension (generator: IntComponent) {
        def within(start: Int, stop: Int): IntComponent =
          generator.within(start, stop)

        def withDefault(default: Int): IntComponent =
          generator.withDefault(default)
      }
    }
}

final case class ColorComponent(label: String, default: Color)
    extends BaseComponent[Color] {
  def withDefault(default: Color): ColorComponent =
    this.copy(default = default)
}
object ColorComponent {
  implicit val exploreColor: ExploreColor[BaseComponent, ColorComponent] =
    new ExploreColor[BaseComponent, ColorComponent] {
      def color(label: String): ColorComponent =
        ColorComponent(label, Color.black)

      extension (generator: ColorComponent) {
        def withDefault(default: Color): ColorComponent =
          generator.withDefault(default)
      }
    }
}
