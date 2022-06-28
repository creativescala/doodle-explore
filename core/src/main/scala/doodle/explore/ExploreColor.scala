package doodle.explore

import java.awt.{Color => AwtColor}
import doodle.core.{Color, UnsignedByte, Normalized}

import fs2.Stream
import fs2.Pure

trait ExploreColor[F[_]] {
  def color(name: String): F[Color]
  def withDefault(generator: F[Color], initValue: Color): F[Color]
}

implicit class ColorComonentOps[F[_]](component: F[Color])(implicit exploreColor: ExploreColor[F]) {
  def withDefault(initValue: Color) = {
    exploreColor.withDefault(component, initValue)
  }
}
