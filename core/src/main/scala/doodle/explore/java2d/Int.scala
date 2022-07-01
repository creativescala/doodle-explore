package doodle.explore.java2d

import doodle.explore.java2d._

import javax.swing._
import fs2.Stream
import fs2.Pure

import doodle.explore.{Explorer, ExploreInt}

final case class IntIR[A](label: String, bounds: Option[(Int, Int)], initial: Int) extends Component[Int] {
  def labelInput[C <: JComponent](label: String, ui: C): JPanel = {
    val panel = new JPanel
    panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))

    val labelComponent = JLabel(label)
    panel.add(labelComponent)
    panel.add(ui)

    panel
  }

  def runAndMakeUI = bounds match {
    case Some((start, end)) =>
      val slider = JSlider(start, end, initial)
      val ui = labelInput(label, slider)
      (ui, Stream(initial).repeat.map(_ => slider.getValue))

    case None =>
      val input = JTextField(initial.toString)
      val ui = labelInput(label, input)
      (ui, Stream(initial).repeat.map(_ => input.getText.toInt))
  }
}

implicit object IntInterpreter extends ExploreInt[IntIR] {
  override def int(label: String) = 
    IntIR(label, None, 0)

  override def within(generator: IntIR[Int], start: Int, end: Int) =
    generator.copy(bounds = Some(start, end), initial = (start + end) / 2)

  override def startingWith(generator: IntIR[Int], newInitial: Int) = 
    generator.copy(initial = newInitial)
}
