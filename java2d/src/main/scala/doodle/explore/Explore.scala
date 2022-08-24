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

package doodle.explore.java2d

import doodle.explore.*

object Explore
    extends BaseConstructor,
      ExploreBooleanConstructor,
      ExploreChoiceConstructor,
      ExploreColorConstructor,
      ExploreIntConstructor {
  type Algebra[F[_]] = ExploreBoolean[F] & ExploreChoice[F] & ExploreColor[F] &
    ExploreInt[F] & Layout[F]

  type Component[A] = doodle.explore.java2d.Component[A]

  object algebraImplementation
      extends ExploreBoolean[Component],
        ExploreChoice[Component],
        ExploreColor[Component],
        ExploreInt[Component],
        Layout[Component] {
    export BooleanInterpreter.*
    export ChoiceInterpreter.*
    export ColorInterpreter.*
    export IntInterpreter.*
    export LayoutInterpreter.*
  }

  given algebra: Algebra[Component] = algebraImplementation
}
