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
trait ExploreInt[F[_]] {
  def int(label: String): F[Int]
  extension (generator: F[Int]) def within(start: Int, end: Int): F[Int]
  extension (generator: F[Int]) def withDefault(initValue: Int): F[Int]

  extension (generator: F[Int])
    def within(range: Range): F[Int] =
      if (range.isInclusive) generator.within(range.start, range.end)
      else generator.within(range.start, range.end - 1)
}
