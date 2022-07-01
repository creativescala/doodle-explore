package doodle.explore.java2d

import doodle.explore.java2d._

import javax.swing._
import fs2.Stream
import fs2.Pure

import java.awt.{Color => AwtColor}
import doodle.core.{Color, UnsignedByte, Normalized}

import doodle.explore.{ExploreColor}

final case class ColorIR[A](name: String, initColor: Color) extends Component[Color] {
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

  def runAndMakeUI = {
    val panel = new JPanel
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))

    val label = JLabel(name)
    val colorPicker = JColorChooser(toAwtColor(initColor))

    panel.add(label)
    panel.add(colorPicker)

    (panel, Stream(initColor).repeat.map(_ => colorPicker.getColor).map(fromAwtColor))
  }
}

implicit object ColorInterpreter extends ExploreColor[ColorIR] {
  override def color(name: String) = 
    ColorIR[Color](name, Color.black.asInstanceOf[Color])

  override def withDefault(generator: ColorIR[Color], initColor: Color) = {
    generator.copy(initColor = initColor)
  }
}
