package doodle.explore.java2d

import doodle.explore.Explorer

import javax.swing._
import fs2.Stream
import fs2.Pure

trait Component[A] extends Explorer[Unit, A] {
  def runAndMakeUI: (JComponent, Stream[Pure, A])

  def run: Stream[Pure, A] = {
    val frame = JFrame("Explorer")
    val (ui, values) = runAndMakeUI
    frame.add(ui)
    frame.setVisible(true)
    frame.pack()

    values
  }
}
