package doodle.explore.java2d

import doodle.explore._
import javax.swing._

implicit object LayoutInterpreter extends Layout[Component] {
  def above[A, B](top: Component[A], bottom: Component[B]) = {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
    panel.add(top.ui)
    panel.add(bottom.ui)
    Component(top.label, panel, top.values.zip(bottom.values))
  }
}
