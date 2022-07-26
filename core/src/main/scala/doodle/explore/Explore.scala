package doodle
package explore

import doodle.core._
import doodle.image._
import doodle.algebra.{Algebra, Picture}
import doodle.image.syntax.all._
import doodle.image.syntax.core._
import doodle.interact.syntax.all._

import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.interact.syntax._

import doodle.java2d.effect.Center._
import doodle.java2d.effect.Redraw._
import doodle.java2d.effect.Size._

import fs2.Stream
import fs2.Pure

import cats.effect.IO
import doodle.interact.effect.AnimationRenderer
import doodle.effect.Renderer

/** An `Explorer[A]` is the base type that describes how to render an explore
  * GUI for a given backend. The reference example is
  * [[doodle.explore.java2d.Component]]
  */
trait Explorer[A, F[_], Alg[x[_]] <: Algebra[x], Canvas, Frame] {

  /** [[run]] instantiates the GUI and returns a stream of its values.
    */
  def run: Stream[Pure, A]

  /** Given a `Frame` and a render function, `explore` runs the explorer GUI,
    * initializes the frame, and produces an animation using the render function
    * and values from the GUI.
    */
  def explore(using
      a: AnimationRenderer[Canvas],
      r: Renderer[Alg, F, Frame, Canvas]
  ) =
    exploreTransformed(identity)

  // /** Like [[explore]], but instead of running `render` directly on the values
  //   * produced by the GUI, [[exploreWithState]] uses the `scanner` function to
  //   * update the `initial` state tick-by-tick.
  //   */
  def exploreWithState[B](initial: B, scanner: (B, A) => B)(using
      a: AnimationRenderer[Canvas],
      r: Renderer[Alg, F, Frame, Canvas]
  ) =
    exploreTransformed { stream =>
      stream.scan(initial)(scanner)
    }

  // https://www.creativescala.org/doodle/api/doodle/interact/syntax/animationrenderersyntax$animatestreamops
  /** [[exploreTransformed]] is a more generic [[explore]] function. It runs
    * `transformer` on the GUI's values before rendering them.
    */
  def exploreTransformed[B](
      transformer: Stream[Pure, A] => Stream[Pure, B]
  )(frame: Frame, render: B => Picture[Alg, F, Unit])(using
      a: AnimationRenderer[Canvas],
      r: Renderer[Alg, F, Frame, Canvas]
  ) = {
    val values = transformer(this.run)
    val frames = values.map(render)

    frame.canvas().flatMap { canvas =>
      frames.animateWithCanvasToIO(canvas)
    }
  }
}
