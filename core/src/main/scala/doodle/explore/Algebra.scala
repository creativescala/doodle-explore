package doodle.explore

import javax.swing._
import javax.swing.event.ChangeListener
import cats.effect.IO

import fs2.Stream

trait GUI[F[_]] {
  def sliderInt(name: String, start: Int, end: Int): F[Int]
}

trait Layout[F[_]] {
  def above[A, B](topComponent: F[A], bottomComponent: F[B]): F[(A, B)]
}

class Component[A](val values: Stream[IO, A], val ui: JComponent) {
  def show = {
    val frame = new JFrame("Explorer")
    frame.add(ui)
    frame.setVisible(true)
    frame.pack()
  }
}

implicit object Java2dGuiInterpreter extends GUI[Component] {
  override def sliderInt(name: String, start: Int, end: Int): Component[Int] = {
    val panel = new JPanel
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS))

    val label = JLabel(name)
    val slider = JSlider(start, end)

    panel.add(label)
    panel.add(slider)

    Component(Stream(slider.getValue).repeat.map(_ => slider.getValue), panel)
  }
}

implicit object Java2dLayoutInterpreter extends Layout[Component] {
  override def above[A, B](topComponent: Component[A], bottomComponent: Component[B]): Component[(A, B)] = {
    val panel = new JPanel
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
    panel.add(topComponent.ui)
    panel.add(bottomComponent.ui)
    panel.setVisible(true)

    Component(topComponent.values.zip(bottomComponent.values), panel)
  }
}

def makeUI(implicit gui: GUI[Component], layout: Layout[Component]) = {
  import gui._
  import layout._

  above(
    sliderInt("Line Width", 0, 10),
    sliderInt("Iterations", 2, 20)
  )
}

// object Main extends App {
//   val ui = makeUI
//   ui.show

//   for (i <- 0 until 10000) {
//     val (lineWidth, iterations) = ui.value()
//     println(s"Line Width: ${lineWidth}, Iterations: ${iterations}")
//     Thread.sleep(1000)
//   }
// }

