package doodle.explore.java2d

import doodle.explore._
import javax.swing._

implicit object LayoutInterpreter extends Layout[Component] {
  def dualBoxLayout(direction: Int, a: JComponent, b: JComponent) = {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, direction))
    panel.add(a)
    panel.add(b)
    panel
  }

  def above[A, B](top: Component[A], bottom: Component[B]) = {
    val ui = dualBoxLayout(BoxLayout.Y_AXIS, top.ui, bottom.ui)
    val label = s"${top.label}, ${bottom.label}"
    Component(label, ui, top.values.zip(bottom.values))
  }

  def beside[A, B](left: Component[A], right: Component[B]) = {
    val ui = dualBoxLayout(BoxLayout.X_AXIS, left.ui, right.ui)
    val label = s"${left.label}, ${right.label}"
    Component(label, ui, left.values.zip(right.values))
  }
}
