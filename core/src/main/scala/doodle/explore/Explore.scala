package doodle.explore

import doodle.core._
import doodle.image._
import doodle.image.syntax._
import doodle.image.syntax.all._
import doodle.image.syntax.core._
import doodle.java2d._

import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.interact.syntax._

import doodle.java2d.effect.Center._
import doodle.java2d.effect.Redraw._
import doodle.java2d.effect.Size._

import fs2.Stream
import fs2.Pure

import cats.effect.IO

trait Explorer[F, A] {
  def show: F
  def values: Stream[Pure, A]

  def explore(frame: Frame, render: A => Picture[Unit]) = {
    this.show

    frame.canvas().flatMap { canvas =>
      val frames: Stream[IO, Picture[Unit]] = this.values.map(render)
      frames.animateWithCanvasToIO(canvas)
    }
  }
}
