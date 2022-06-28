package doodle.explore.java2d

import javax.swing._
import fs2.Stream
import fs2.Pure

class Component[A](
  val label: String,
  val ui: JComponent,
  val values: Stream[Pure, A]
) {
  def show() = {
    val frame = JFrame("Explorer")
    frame.add(ui)
    frame.setVisible(true)
    frame.pack()
  }
}
