package doodle.explore

import java.awt.{Color => AwtColor}
import doodle.core.{Color, UnsignedByte, Normalized}

import fs2.Stream
import fs2.Pure

/** Describes a DSL for exploring a color. These functions can be used with dot
  * or infix notation through [[ColorComponentOps]].
  */
trait ExploreColor[F[_]] {
  def color(name: String): F[Color]
  def withDefault(generator: F[Color], initValue: Color): F[Color]
}

/** Extension methods for [[ExploreColor]] GUI elements, so that they can be
  * used with dot or infix notation.
  */
trait ColorComponentOps {
  extension [F[_]](component: F[Color])(using exploreColor: ExploreColor[F]) {
    def withDefault(initValue: Color) =
      exploreColor.withDefault(component, initValue)
  }
}
