package example

import doodle.core.*
import doodle.syntax.all.*
import doodle.explore.*
import doodle.explore.laminar.{Component, IntInterpreter}
import doodle.svg.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("Example")
object Example {
  def concentricCircles(count: Int): Picture[Unit] =
    if count == 0 then Picture.empty
    else Picture.circle(count * 20).on(concentricCircles(count - 1))

  // Explore the concentricCircles example
  //
  // mountId is the ID of the div where the animation should be displayed
  @JSExport
  def go(mountId: String): Unit = {
    val frame = Frame(mountId)
    val initialCount = 4

    IntInterpreter
      .int("Count")
      .within(1, 20)
      .withDefault(initialCount)
      .explore(
        frame,
        count => concentricCircles(count)
      )
  }
}
