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
import doodle.explore.generic.{IntComponent, ColorComponent}
import doodle.java2d.{Algebra, Drawing, Frame, Canvas}

object Explore
    extends ExploreIntConstructor(IntComponent.exploreInt)
    with ExploreColorConstructor(ColorComponent.exploreColor) {
  export Component.Component

  export doodle.explore.generic.IntComponent.given ExploreInt
  export doodle.explore.generic.IntComponent.given ExploreColor
  export doodle.explore.generic.Layout.layout
  export Component.given Explorer[Component, Algebra, Drawing, Frame, Canvas]

}
