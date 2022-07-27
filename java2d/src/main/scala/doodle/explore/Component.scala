package doodle.explore.java2d

import doodle.explore.Explorer

import javax.swing._
import fs2.Stream
import fs2.Pure

import doodle.explore.{ExploreInt, ExploreColor, ExploreChoice, Layout}
import doodle.core.{Color, UnsignedByte, Normalized}
import java.awt.{Color => AwtColor}
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import doodle.java2d
import doodle.explore.ExploreButton
import doodle.explore.LayoutDirection

/** An explore GUI element for the Java2D backend
  */
enum Component[A]
    extends Explorer[
      A,
      java2d.Drawing,
      java2d.Algebra,
      java2d.Canvas,
      java2d.Frame
    ] {
  case IntIR(label: String, bounds: Option[(Int, Int)], initial: Int)
      extends Component[Int]
  case ColorIR(label: String, initColor: Color) extends Component[Color]
  case ButtonIR(label: String) extends Component[Boolean]
  case ChoiceIR[A](label: String, choices: Seq[A], choiceLabels: Seq[String])
      extends Component[A]
  case LayoutIR[A, B](direction: LayoutDirection, a: Component[A], b: Component[B])
      extends Component[(A, B)]

  private def toAwtColor(color: Color) = {
    val rgba = color.toRGBA
    AwtColor(rgba.r.get, rgba.g.get, rgba.b.get, rgba.a.toUnsignedByte.get)
  }

  private def fromAwtColor(color: java.awt.Color) = Color.RGBA(
    UnsignedByte((color.getRed - 128).toByte),
    UnsignedByte((color.getGreen - 128).toByte),
    UnsignedByte((color.getBlue - 128).toByte),
    Normalized(color.getAlpha.toDouble / 255.0)
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

      (
        panel,
        Stream(initial).repeat.map(_ => colorPicker.getColor).map(fromAwtColor)
      )

    case ButtonIR(label) =>
      val button = JButton(label)

      var pressed = false
      object Listener extends ActionListener {
        def actionPerformed(_e: ActionEvent) = {
          pressed = true
        }
      }
      button.addActionListener(Listener)

      (
        button,
        Stream(pressed).repeat.map(_ => {
          val wasPressed = pressed
          pressed = false
          wasPressed
        })
      )

    case ChoiceIR(name, choices, labels) =>
      import collection.JavaConverters.seqAsJavaListConverter
      val panel = new JPanel
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))

      val label = JLabel(name)
      val comboBox = JComboBox(new java.util.Vector(labels.asJava))

      panel.add(label)
      panel.add(comboBox)

      val labelToChoice = labels.zip(choices).toMap

      (
        panel,
        Stream(choices(0)).repeat
          .map(_ => comboBox.getSelectedItem.asInstanceOf[String])
          .map(labelToChoice)
      )

    case LayoutIR(direction, a, b) =>
      val (aUI, aValues) = a.runAndMakeUI
      val (bUI, bValues) = b.runAndMakeUI
      val directionInt = direction match {
        case LayoutDirection.Horizontal => BoxLayout.X_AXIS
        case LayoutDirection.Vertical => BoxLayout.Y_AXIS,
      }
      (dualBoxLayout(directionInt, aUI, bUI), aValues.zip(bValues))
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
  import Component.{IntIR, ChoiceIR}

  override def int(label: String) =
    IntIR(label, None, 0)

  override def within(generator: Component[Int], start: Int, end: Int) =
    generator match {
      case generator: IntIR =>
        generator.copy(bounds = Some(start, end), initial = (start + end) / 2)
      case _ => ???
    }

  override def startingWith(generator: Component[Int], newInitial: Int) =
    generator match {
      case generator: IntIR         => generator.copy(initial = newInitial)
      case generator: ChoiceIR[Int] => ???
    }
}

implicit object ChoiceInterpreter extends ExploreChoice[Component] {
  import Component.ChoiceIR

  override def choice[A](label: String, choices: Seq[A]) =
    ChoiceIR(label, choices, choices.map(_.toString))

  override def labeledChoice[A](label: String, choices: Seq[(String, A)]) =
    ChoiceIR(label, choices.map(_._2), choices.map(_._1))
}

implicit object ColorInterpreter extends ExploreColor[Component] {
  import Component.{ColorIR, ChoiceIR}

  override def color(name: String) =
    ColorIR(name, Color.black.asInstanceOf[Color])

  override def withDefault(generator: Component[Color], initColor: Color) =
    generator match {
      case generator: ColorIR         => generator.copy(initColor = initColor)
      case generator: ChoiceIR[Color] => ???
    }
}

implicit object ButtonInterpreter extends ExploreButton[Component] {
  import Component.ButtonIR

  def button(label: String) = ButtonIR(label)
}

implicit object LayoutInterpreter extends Layout[Component] {
  import Component.LayoutIR

  def above[A, B](top: Component[A], bottom: Component[B]) =
    LayoutIR(LayoutDirection.Vertical, top, bottom)

  def beside[A, B](left: Component[A], right: Component[B]) =
    LayoutIR(LayoutDirection.Horizontal, left, right)
}
