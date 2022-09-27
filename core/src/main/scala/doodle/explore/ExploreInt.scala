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

/** Describes a DSL for exploring an integer value. These functions can be used
  * with dot or infix notation through [[IntComponentOps]].
  */
trait ExploreInt[F[_], Component <: F[Int]] {
  def int(label: String): Component
  extension (generator: Component) def within(start: Int, end: Int): Component
  extension (generator: Component) def withDefault(initValue: Int): Component

  extension (generator: Component)
    def within(range: Range): Component =
      if (range.isInclusive) generator.within(range.start, range.end)
      else generator.within(range.start, range.end - 1)
}

trait ExploreIntConstructor[F[_], Component <: F[Int]](
    algebra: ExploreInt[F, Component]
) {
  def int(label: String): Component =
    algebra.int(label)
}
