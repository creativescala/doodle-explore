package doodle.explore

import javax.swing._
import javax.swing.event.ChangeListener
import cats.effect.IO

import doodle.core.{Color, UnsignedByte, Normalized}

import fs2.Stream
import fs2.Pure

trait GUI[F[_]] {
  def sliderInt(name: String, start: Int, end: Int, initValue: Int): F[Int]
  def colorPicker(name: String, initColor: Color): F[Color]
}

trait Layout[F[_]] {
  def above[A, B](topComponent: F[A], bottomComponent: F[B]): F[(A, B)]
}

class Component[A](val values: Stream[Pure, A], val ui: JComponent) {
  def show = {
    val frame = new JFrame("Explorer")
    frame.add(ui)
    frame.setVisible(true)
    frame.pack()
  }
}

implicit object Java2dGuiInterpreter extends GUI[Component] {
  override def sliderInt(name: String, start: Int, end: Int, initValue: Int): Component[Int] = {
    val panel = new JPanel
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS))

    val label = JLabel(name)
    val slider = JSlider(start, end, initValue)
    slider.setSnapToTicks(true)

    panel.add(label)
    panel.add(slider)

    // TODO: There must be a better way to make an infinite stream of values
    Component(Stream(0).repeat.map(_ => slider.getValue), panel)
  }

  override def colorPicker(name: String, initColor: Color): Component[Color] = {
    val panel = new JPanel
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))

    val label = JLabel(name)

    val toPercent = (byte: UnsignedByte) => byte.get.toFloat / 255f
    val toJavaColor = (color: Color) => java.awt.Color(
      toPercent(color.red), toPercent(color.green), toPercent(color.blue)
    )
    val fromJavaColor = (color: java.awt.Color) => Color.RGBA(
      UnsignedByte((color.getRed - 128).toByte), 
      UnsignedByte((color.getGreen - 128).toByte),
      UnsignedByte((color.getBlue - 128).toByte),
      Normalized(color.getAlpha.toDouble / 255.0),
    )

    val jcolor = java.awt.Color(toPercent(initColor.red), toPercent(initColor.green), toPercent(initColor.blue))
    val colorPicker = JColorChooser(jcolor)

    panel.add(label)
    panel.add(colorPicker)

    Component(Stream(0).repeat.map(_ => fromJavaColor(colorPicker.getColor)), panel)
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
