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

/** Base module for constructors
  *
  * Most users will only use a single backend. Abstracting over backends is
  * useful for Doodle developers but not so useful for the typical user.
  *
  * To improve the experience for a typical user we provide mixins that require
  * the below interface, and construct a concrete implementation for each
  * backend. This allows users to access constructors without having to specify
  * given instances, reducing the complexity of using the library.
  */
trait BaseConstructor {

  /** The type of all algebras a given concrete implementation implements. */
  type Algebra[F[_]]

  /** The type of UI components that a given concrete implementation produces.
    */
  type Component[A]

  /** The given instance implementing all the algebras. */
  given algebra: Algebra[Component]

}
