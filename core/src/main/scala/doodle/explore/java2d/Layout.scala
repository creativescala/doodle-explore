// package doodle.explore.java2d

// import doodle.explore._
// import javax.swing._

// final case class LayoutIR[A, B](direction: Int, a: Component[A], b: Component[B]) extends Component[(A, B)] {
//   def dualBoxLayout(direction: Int, a: JComponent, b: JComponent) = {
//     val panel = JPanel()
//     panel.setLayout(BoxLayout(panel, direction))
//     panel.add(a)
//     panel.add(b)
//     panel
//   }

//   def runAndMakeUI = {
//     val (aUI, aValues) = a.runAndMakeUI
//     val (bUI, bValues) = b.runAndMakeUI
//     (dualBoxLayout(direction, aUI, bUI), aValues.zip(bValues))
//   }
// }

// implicit object LayoutInterpreter extends Layout[Component] {
//   def above[A, B](top: Component[A], bottom: Component[B]) =
//     LayoutIR(BoxLayout.Y_AXIS, top, bottom)

//   def beside[A, B](left: Component[A], right: Component[B]) =
//     LayoutIR(BoxLayout.X_AXIS, left, right)
// }
