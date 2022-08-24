package example

import doodle.core.*
import doodle.syntax.all.*
import doodle.explore.*
import doodle.explore.laminar.*
import doodle.svg.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("Example")
object Example {
  def concentricCircles(count: Int, color: Color): Picture[Unit] =
    if count == 0 then Picture.empty
    else
      Picture
        .circle(count * 20)
        .fillColor(color.spin(10.degrees * count))
        .on(concentricCircles(count - 1, color))

  // Explore the concentricCircles example
  //
  // mountId is the ID of the div where the animation should be displayed
  @JSExport
  def go(mountId: String): Unit = {
    val frame = Frame(mountId)
    val initialCount = 4
    val initialColor = Color.midnightBlue

    IntInterpreter
      .int("Count")
      .within(1, 20)
      .withDefault(initialCount)
      .above(ColorInterpreter.color("Color"))
      .explore(
        frame,
        (count, color) => concentricCircles(count, color)
      )
  }
}
