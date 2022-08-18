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
  extension (generator: F[Color]) def withDefault(initValue: Color): F[Color]
}
