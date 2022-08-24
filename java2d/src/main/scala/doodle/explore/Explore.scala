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
