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

/** Describes a DSL for setting the layout of a given [[Explorer]] component.
  * These functions can be used with dot or infix notation through
  * [[LayoutOps]].
  */
trait Layout[F[_]] {
  extension [A, B](top: F[A]) def above(bottom: F[B]): F[(A, B)]
  extension [A, B](left: F[A]) def beside(right: F[B]): F[(A, B)]
}
