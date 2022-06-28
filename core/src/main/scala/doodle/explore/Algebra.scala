package doodle.explore

import javax.swing._
import javax.swing.event.ChangeListener
import cats.effect.IO

import java.awt.{Color => AwtColor}

import doodle.core.{Color, UnsignedByte, Normalized}

import fs2.Stream
import fs2.Pure

trait ExploreInt[F[_], S <: F[Int]] {
  def int(label: String): F[Int]
  def within(generator: F[Int], start: Int, end: Int): S
  def startingWith(generator: S, initValue: Int): S
}

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

class IntSlider(
  override val label: String, 
  override val ui: JComponent, 
  override val values: Stream[Pure, Int], 
  val start: Int, 
  val end: Int, 
  val initValue: Int
) extends Component[Int](label, ui, values)

implicit object IntInterpreter extends ExploreInt[Component, IntSlider] {
  def labelInput[A <: JComponent](label: String, ui: A): JPanel = {
    val panel = new JPanel
    panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))

    val labelComponent = JLabel(label)
    panel.add(labelComponent)
    panel.add(ui)

    panel
  }

  override def int(label: String) = {
    val input = JTextField("0")
    val ui = labelInput(label, input)

    Component(label, ui, Stream(0).repeat.map(_ => input.getText.toInt))
  }

  override def within(generator: Component[Int], start: Int, end: Int) = {
    val slider = JSlider(start, end)
    val ui = labelInput(generator.label, slider)

    IntSlider(generator.label, ui, Stream(0).repeat.map(_ => slider.getValue), start, end, (start + end) / 2)
  }

  override def startingWith(generator: IntSlider, initValue: Int) = {
    val slider = JSlider(generator.start, generator.end, initValue)
    val ui = labelInput(generator.label, slider)

    IntSlider(generator.label, ui, Stream(0).repeat.map(_ => slider.getValue), generator.start, generator.end, initValue)
  }
}

implicit class IntComponentOps[F[_], S <: F[Int]](component: F[Int])(implicit exploreInt: ExploreInt[F, S]) {
  def within(start: Int, end: Int) = {
    exploreInt.within(component, start, end)
  }
}

implicit class IntSliderOps[F[_], S <: F[Int]](component: S)(implicit exploreInt: ExploreInt[F, S]) {
  def startingWith(initValue: Int) = {
    exploreInt.startingWith(component, initValue)
  }
}

trait Layout[F[_]] {
  def above[A, B](top: F[A], bottom: F[B]): F[(A, B)]
}

implicit object LayoutInterpreter extends Layout[Component] {
  def above[A, B](top: Component[A], bottom: Component[B]) = {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
    panel.add(top.ui)
    panel.add(bottom.ui)
    Component(top.label, panel, top.values.zip(bottom.values))
  }
}

implicit class LayoutOps[F[_], A](component: F[A])(implicit layout: Layout[F]) {
  def above[B](other: F[B]) = {
    layout.above(component, other)
  }
}

// trait Layout[F[_]] {
//   def above[A, B, C[_] <: F[_], D[_] <: F[_]](top: C[A], bottom: D[B]): F[(A, B)]
// }

// implicit object LayoutInterpreter extends Layout[Component] {
//   def above[A, B, C[_] <: Component[_], D[_] <: Component[_]](top: C[A], bottom: D[B]) = {
//     val panel = JPanel()
//     panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
//     panel.add(top.ui)
//     panel.add(bottom.ui)
//     Component(top.label, panel, top.values.zip(bottom.values))
//   }
// }

// implicit class LayoutOps[A, C[_] <: F[_], F[_]](component: C[A])(implicit layout: Layout[F]) {
//   def above[B, D[_] <: F[_]](other: D[B]) = {
//     layout.above(component, other)
//   }
// }
