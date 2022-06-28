package doodle.explore.java2d

import doodle.explore.java2d._

import javax.swing._
import fs2.Stream
import fs2.Pure

import doodle.explore.{ExploreInt}

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
