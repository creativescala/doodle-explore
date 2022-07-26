package doodle.explore.laminar

import cats.effect.{IO, IOApp}
import cats.effect.IO.asyncForIO
import cats.effect.IO

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import doodle.explore.{ExploreInt, IntComponentOps}
import doodle.explore.laminar.Component

object Main {
  def explorer(using intGui: ExploreInt[Component]) = {
    import intGui._

    int("Test")
  }

  def main(args: Array[String]): Unit = {
    explorer.run.take(100).map(x => println(x)).compile.drain
  }
}
