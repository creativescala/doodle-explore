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

package doodle.explore.laminar

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import fs2.Stream
import fs2.Pure

import doodle.explore.*
import doodle.explore.generic.*
import doodle.effect.Renderer
import doodle.interact.effect.AnimationRenderer
import scala.concurrent.duration.DurationInt
import cats.effect.GenTemporal
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Promise
import java.util.concurrent.Future

import doodle.svg.{Drawing, Algebra, Canvas, Frame}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html.{Div => SJSDiv}
import doodle.algebra.Picture
import doodle.explore.LayoutDirection
import doodle.explore.Layout
import doodle.core.Color
import doodle.explore.ExploreColor
import doodle.core.UnsignedByte
import doodle.explore.ExploreBoolean
import doodle.explore.Choice

object Component {
  type Component[A] = BaseComponent[A]

  given laminarExplorer: Explorer[Component, Algebra, Drawing, Frame, Canvas]
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

  def makeUi[A](
      component: Component[A]
  ): (Stream[Pure, A], ReactiveHtmlElement[SJSDiv]) =
    this match {
      case IntComponent(label, range, default) =>
        val currentValue = Var(default.toString)
        val (min, max) = range.getOrElse(0, 100)
        val app = div(
          span(label),
          input(
            typ("range"),
            minAttr(min.toString),
            maxAttr(max.toString),
            defaultValue(default.toString),
            onInput.mapToValue --> currentValue
          )
        )

        val values = Stream(default).repeat.map(_ => currentValue.now().toInt)
        (values, app)

      case ColorComponent(label, default) =>
        def colorToHex(color: Color) = {
          val toHex = (c: UnsignedByte) => {
            val str = c.get.toLong.toHexString
            if (str.length == 1)
              s"0${str}"
            else
              str
          }
          val alpha = toHex(color.alpha.toUnsignedByte)
          s"#${toHex(color.red)}${toHex(color.green)}${toHex(color.blue)}${alpha}"
        }

        def hexToColor(s: String) = {
          val h = "[0-9a-fA-F]"
          val rgb = s"#(${h}{2})(${h}{2})(${h}{2})".r
          val rgba = s"#(${h}{2})(${h}{2})(${h}{2})(${h}{2})".r

          val hexToBytes = (hex: String) => {
            val int = BigInt(Integer.parseInt(hex, 16))
            val res: Array[Int] = int.toByteArray.map {
              case x if x < 0 => 256 + x
              case x          => x
            }
            res
          }

          s match {
            case rgb(rStr, gStr, bStr) =>
              val Array(r, g, b) =
                Array(rStr, gStr, bStr).map(hexToBytes).map(_.last)
              Color.rgb(r, g, b)

            case rgba(rStr, gStr, bStr, aStr) =>
              val Array(r, g, b, a) =
                Array(rStr, gStr, bStr, aStr).map(hexToBytes).map(_.last)
              Color.rgba(r, g, b, a.toDouble / 255.0)
          }
        }

        val currentValue = Var(colorToHex(default))
        val app = div(
          span(label),
          input(typ := "color", onInput.mapToValue --> currentValue),
          span(child.text <-- currentValue)
        )

        val values = Stream(default).repeat.map(_ => {
          // println(s"here: ${currentValue.now()}, ${hexToColor(currentValue.now())}")
          // println(hexToColor(currentValue.now()))
          hexToColor(currentValue.now())
        })
        (values, app)

      //   case BooleanIR(label, true) =>
      //     val currentValue = Var(false)
      //     val app = div(button(onClick.mapTo(true) --> currentValue, label))

      //     val values = Stream(false).repeat.map(_ => {
      //       val wasPressed = currentValue.now()
      //       currentValue.set(false)
      //       wasPressed
      //     })

      //     (values, app)

      //   case BooleanIR(labelText, false) =>
      //     val currentValue = Var(false)
      //     val app = div(
      //       label(labelText),
      //       input(typ := "checkbox", onChange.mapToChecked --> currentValue)
      //     )

      //     val values = Stream(false).repeat.map(_ => currentValue.now())
      //     (values, app)

      //   case ChoiceIR(label, choices, choiceLabels) =>
      //     val currentValue = Var(choiceLabels(0))
      //     val lastValidValue = Var(choiceLabels(0))
      //     val labelToChoice = choiceLabels.zip(choices).toMap
      //     val app = div(
      //       span(label),
      //       select(
      //         inContext { node =>
      //           onChange.mapTo(node.ref.value) --> currentValue.writer
      //         },
      //         choiceLabels.map(label => option(value := label, label))
      //       )
      //     )

      //     val values = Stream(choices(0)).repeat.map { _ =>
      //       val label = currentValue.now() match {
      //         case null => lastValidValue.now()
      //         case value => {
      //           lastValidValue.set(value)
      //           value
      //         }
      //       }

      //       labelToChoice(label)
      //     }

      //     (values.map(Choice(_)), app)

      //   case LayoutIR(direction, a, b) =>
      //     val (aValues, aUI) = a.runAndMakeUI
      //     val (bValues, bUI) = b.runAndMakeUI

      //     val class_ = direction match {
      //       case LayoutDirection.Horizontal => "horizontal"
      //       case LayoutDirection.Vertical   => "vertical"
      //     }
      //     val ui = div(className := class_, aUI, bUI)
      //     val values = aValues.zip(bValues)
      //     (values, ui)
    }

  def run[A](component: Component[A]): Stream[Pure, A] = {
    val (values, ui) = makeUi(component)
    val container = dom.document.querySelector("#explorer")

    render(container, ui)

    values
  }

  def run[A](containerId: String, component: Component[A]): Stream[Pure, A] = {
    val (values, ui) = makeUi(component)
    val container = dom.document.querySelector(containerId)

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(container, ui)
    }(unsafeWindowOwner)

    values
  }
}
