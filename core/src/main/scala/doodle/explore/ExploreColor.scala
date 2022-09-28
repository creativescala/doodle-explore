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

import doodle.core.Color

/** Describes a DSL for exploring a color. These functions can be used with dot
  * or infix notation through [[ColorComponentOps]].
  */
trait ExploreColor[F[_], Component <: F[Color]] {
  def color(label: String): Component
  extension (generator: Component) def withDefault(initValue: Color): Component
}

trait ExploreColorConstructor[F[_], Component <: F[Color]](
    algebra: ExploreColor[F, Component]
) {
  def color(label: String): Component =
    algebra.color(label)
}
