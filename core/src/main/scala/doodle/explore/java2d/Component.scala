package doodle.explore.java2d

import doodle.explore.Explorer

import javax.swing._
import fs2.Stream
import fs2.Pure

import doodle.explore.{ExploreInt, ExploreColor, Layout}
import doodle.core.{Color, UnsignedByte, Normalized}
import java.awt.{Color => AwtColor}


enum Component[A] extends Explorer[Unit, A] {
  case IntIR(label: String, bounds: Option[(Int, Int)], initial: Int) extends Component[Int]
  case ColorIR(label: String, initColor: Color) extends Component[Color]
  case LayoutIR[A, B](direction: Int, a: Component[A], b: Component[B]) extends Component[(A, B)]

  private def toAwtColor(color: Color) = {
    val rgba = color.toRGBA
    AwtColor(rgba.r.get, rgba.g.get, rgba.b.get, rgba.a.toUnsignedByte.get)
  }

  private def fromAwtColor(color: java.awt.Color) = Color.RGBA(
    UnsignedByte((color.getRed - 128).toByte), 
    UnsignedByte((color.getGreen - 128).toByte),
    UnsignedByte((color.getBlue - 128).toByte),
    Normalized(color.getAlpha.toDouble / 255.0),
    )

  private def labelInput(label: String, ui: JComponent): JPanel = {
    val panel = new JPanel
    panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))

    val labelComponent = JLabel(label)
    panel.add(labelComponent)
    panel.add(ui)

    panel
  }

  private def dualBoxLayout(direction: Int, a: JComponent, b: JComponent) = {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, direction))
    panel.add(a)
    panel.add(b)
    panel
  }

  def runAndMakeUI: (JComponent, Stream[Pure, A]) = this match {
    case IntIR(label, None, initial) =>
      val input = JTextField(initial.toString)
      val ui = labelInput(label, input)
      (ui, Stream(initial).repeat.map(_ => input.getText.toInt))

    case IntIR(label, Some((start, end)), initial) =>
      val slider = JSlider(start, end, initial)
      val ui = labelInput(label, slider)
      (ui, Stream(initial).repeat.map(_ => slider.getValue))

    case ColorIR(name, initial) =>
      val panel = new JPanel
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))

      val label = JLabel(name)
      val colorPicker = JColorChooser(toAwtColor(initial))

      panel.add(label)
      panel.add(colorPicker)

      (panel, Stream(initial).repeat.map(_ => colorPicker.getColor).map(fromAwtColor))

    case LayoutIR(direction, a, b) =>
      val (aUI, aValues) = a.runAndMakeUI
      val (bUI, bValues) = b.runAndMakeUI
      (dualBoxLayout(direction, aUI, bUI), aValues.zip(bValues))
  }

  def run: Stream[Pure, A] = {
    val frame = JFrame("Explorer")
    val (ui, values) = runAndMakeUI

    frame.add(ui)
    frame.setVisible(true)
    frame.pack()
    values
  }
}

implicit object IntInterpreter extends ExploreInt[Component] {
  import Component.IntIR

  override def int(label: String) = 
    IntIR(label, None, 0)

  override def within(generator: Component[Int], start: Int, end: Int) = generator match {
    case generator: IntIR => generator.copy(bounds = Some(start, end), initial = (start + end) / 2)
  }

  override def startingWith(generator: Component[Int], newInitial: Int) = generator match {
    case generator: IntIR => generator.copy(initial = newInitial)
  }
}

implicit object ColorInterpreter extends ExploreColor[Component] {
  import Component.ColorIR

  override def color(name: String) = 
    ColorIR(name, Color.black.asInstanceOf[Color])

  override def withDefault(generator: Component[Color], initColor: Color) = generator match {
    case generator: ColorIR => generator.copy(initColor = initColor)
  }
}

implicit object LayoutInterpreter extends Layout[Component] {
  import Component.LayoutIR

  def above[A, B](top: Component[A], bottom: Component[B]) =
    LayoutIR(BoxLayout.Y_AXIS, top, bottom)

  def beside[A, B](left: Component[A], right: Component[B]) =
    LayoutIR(BoxLayout.X_AXIS, left, right)
}
