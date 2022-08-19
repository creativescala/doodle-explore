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

trait ExploreBoolean[F[_]] {
  def boolean(label: String): F[Boolean]

  def asButton(generator: F[Boolean]): F[Boolean]
  def asCheckbox(generator: F[Boolean]): F[Boolean]

  def button(label: String) = asButton(boolean(label))
  def checkbox(label: String) = asCheckbox(boolean(label))
}
