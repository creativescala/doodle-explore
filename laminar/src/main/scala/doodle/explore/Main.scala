package doodle.explore.laminar

import cats.effect.{IO, IOApp}
import cats.effect.IO.asyncForIO
import cats.effect.IO

import scala.concurrent.duration.DurationInt

// import doodle.svg.effect.Center._
// import doodle.svg.effect.Redraw._
// import doodle.svg.effect.Size._

import doodle.core._
import doodle.image._
import doodle.image.syntax._
import doodle.image.syntax.all._
import doodle.image.syntax.core._

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import doodle.explore.{ExploreInt, IntComponentOps}
import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.explore.laminar.Component

import doodle.interact.effect.AnimationRenderer
import doodle.effect.Renderer
import doodle.svg.effect.Frame
import doodle.svg.effect.Size.FixedSize

object Main {
  def explorer(using intGui: ExploreInt[Component]) = {
    import intGui._

    int("Test")
  }

  def main(args: Array[String]): Unit = {
    import org.scalajs.dom
    import org.scalajs.dom.document
    import cats.effect.unsafe.implicits.global
    val frame = Frame("doodle")

    import doodle.svg.svgAnimationRenderer
    import doodle.svg.svgRenderer
    explorer.explore(frame, radius => Image.compile {
      Image.circle(radius).strokeColor(Color.black).strokeWidth(10.0)
    })
  }
}
