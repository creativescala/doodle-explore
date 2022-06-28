package doodle.explore.java2d

import doodle.explore.java2d._

import javax.swing._
import fs2.Stream
import fs2.Pure

import java.awt.{Color => AwtColor}
import doodle.core.{Color, UnsignedByte, Normalized}

import doodle.explore.{ExploreColor}

implicit object ColorInterpreter extends ExploreColor[Component] {
  def toAwtColor(color: Color) = {
    val rgba = color.toRGBA
    AwtColor(rgba.r.get, rgba.g.get, rgba.b.get, rgba.a.toUnsignedByte.get)
  }

  def fromAwtColor(color: java.awt.Color) = Color.RGBA(
    UnsignedByte((color.getRed - 128).toByte), 
    UnsignedByte((color.getGreen - 128).toByte),
    UnsignedByte((color.getBlue - 128).toByte),
    Normalized(color.getAlpha.toDouble / 255.0),
    )

  def colorPickerComponent(name: String, initColor: Color): Component[Color] = {
    val panel = new JPanel
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))

    val label = JLabel(name)
    val colorPicker = JColorChooser(toAwtColor(initColor))

    panel.add(label)
    panel.add(colorPicker)

    Component(name, panel, Stream(0).repeat.map(_ => colorPicker.getColor).map(fromAwtColor))
  }

  override def color(name: String) = colorPickerComponent(name, Color.black.asInstanceOf[Color])

  override def withDefault(generator: Component[Color], initColor: Color) = {
    colorPickerComponent(generator.label, initColor)
  }
}
