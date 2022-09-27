/*
 * Copyright 2022 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doodle.explore.java2d

import doodle.core.{Color, Normalized, UnsignedByte}
import doodle.explore.*
import doodle.explore.generic.*
import doodle.algebra.Picture
import doodle.java2d.{Algebra, Drawing, Frame, Canvas}
import fs2.{Pure, Stream}
import javax.swing.*
import java.awt.{Color => AwtColor}
import doodle.interact.effect.AnimationRenderer
import doodle.effect.Renderer
import cats.effect.unsafe.implicits.global

object Component {
  type Component[A] = BaseComponent[A]

  given java2dExplorer: Explorer[Component, Algebra, Drawing, Frame, Canvas]
    with {
    extension [A](component: Component[A]) {
      def explore(frame: Frame)(render: A => Picture[Algebra, Drawing, Unit])(
          using
          a: AnimationRenderer[Canvas],
          r: Renderer[Algebra, Component, Frame, Canvas]
      ): Unit = ???

      def exploreScan[B](
          frame: Frame
      )(
          initial: B
      )(scan: (B, A) => B)(render: B => Picture[Algebra, Drawing, Unit])(using
          a: AnimationRenderer[Canvas],
          r: Renderer[Algebra, Component, Frame, Canvas]
      ): Unit = {
        val frames = run(component).scan(initial)(scan).map(render)
        (frame
          .canvas()
          .flatMap { canvas =>
            frames.animateWithCanvasToIO(canvas)
          })
          .unsafeRunAsync(x => System.err.println(x))
      }
    }
  }

  def toAwtColor(color: Color) = {
    val rgba = color.toRGBA
    AwtColor(rgba.r.get, rgba.g.get, rgba.b.get, rgba.a.toUnsignedByte.get)
  }

  def fromAwtColor(color: java.awt.Color) = Color.RGBA(
    UnsignedByte((color.getRed - 128).toByte),
    UnsignedByte((color.getGreen - 128).toByte),
    UnsignedByte((color.getBlue - 128).toByte),
    Normalized(color.getAlpha.toDouble / 255.0)
  )

  def labelInput(label: String, ui: JComponent): JPanel = {
    val panel = new JPanel
    panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))

    val labelComponent = JLabel(label)
    panel.add(labelComponent)
    panel.add(ui)

    panel
  }

  def makeUi[A](component: Component[A]): (JComponent, Stream[Pure, A]) =
    component match {
      case IntComponent(label, None, default) =>
        val input = JTextField(default.toString)
        val ui = labelInput(label, input)
        (ui, Stream(default).repeat.map(_ => input.getText.toInt))

      case IntComponent(label, Some(start, stop), default) =>
        val slider = JSlider(start, stop, default)
        val ui = labelInput(label, slider)
        (ui, Stream(default).repeat.map(_ => slider.getValue))

      case ColorComponent(label, default) =>
        val colorPicker = JColorChooser(toAwtColor(default))
        val ui = labelInput(label, colorPicker)

        val stream = Stream(default).repeat
          .map(_ => colorPicker.getColor)
          .map(fromAwtColor)

        (ui, stream)
    }

  def run[A](component: Component[A]): Stream[Pure, A] = {
    val frame = JFrame("Explorer")
    val (ui, values) = makeUi(component)

    frame.add(ui)
    frame.setVisible(true)
    frame.pack()
    values
  }

}
