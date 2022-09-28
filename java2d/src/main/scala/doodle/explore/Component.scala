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

import cats.effect.unsafe.implicits.global
import doodle.algebra.Picture
import doodle.core.Color
import doodle.core.Normalized
import doodle.core.UnsignedByte
import doodle.effect.Renderer
import doodle.explore.*
import doodle.explore.generic.*
import doodle.interact.effect.AnimationRenderer
import doodle.interact.syntax.all.*
import doodle.java2d.*
import doodle.syntax.all.*
import fs2.Pure
import fs2.Stream

import java.awt.{Color => AwtColor}
import javax.swing.*

object Component {
  type Component[A] = BaseComponent[A]

  given java2dExplorer: Explorer[Component, Algebra, Drawing, Frame, Canvas]
    with {
    extension [A](component: Component[A]) {
      def explore(frame: Frame)(render: A => Picture[Algebra, Drawing, Unit])(
          using
          a: AnimationRenderer[Canvas],
          r: Renderer[Algebra, Drawing, Frame, Canvas]
      ): Unit = {
        val frames = run(component).map(render)
        (frame
          .canvas()
          .flatMap { canvas =>
            frames.animateWithCanvasToIO(canvas)
          })
          .unsafeRunAsync(x => System.err.println(x))
      }

      def exploreScan[B](
          frame: Frame
      )(
          initial: B
      )(scan: (B, A) => B)(render: B => Picture[Algebra, Drawing, Unit])(using
          a: AnimationRenderer[Canvas],
          r: Renderer[Algebra, Drawing, Frame, Canvas]
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

  private def dualBoxLayout(direction: Int, a: JComponent, b: JComponent) = {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, direction))
    panel.add(a)
    panel.add(b)
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

      case Above(top, bottom) =>
        val (aUI, aValues) = makeUi(top)
        val (bUI, bValues) = makeUi(bottom)
        (dualBoxLayout(BoxLayout.Y_AXIS, aUI, bUI), aValues.zip(bValues))

      case Beside(left, right) =>
        val (aUI, aValues) = makeUi(left)
        val (bUI, bValues) = makeUi(right)
        (dualBoxLayout(BoxLayout.X_AXIS, aUI, bUI), aValues.zip(bValues))
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
