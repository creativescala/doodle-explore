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

import doodle.explore.Explorer
import doodle.explore.{ExploreInt, ExploreBoolean, ExploreChoice}

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
import doodle.explore.ChoiceOps._

enum Component[A] extends Explorer[A, Drawing, Algebra, Canvas, Frame] {
  case IntIR(label: String, bounds: Option[(Int, Int)], initial: Int)
      extends Component[Int]
  case ColorIR(label: String, initColor: Color) extends Component[Color]
  case BooleanIR(label: String, isButton: Boolean) extends Component[Boolean]
  case ChoiceIR[A](label: String, choices: Seq[A], choiceLabels: Seq[String])
      extends Component[A]
  case LayoutIR[A, B](
      direction: LayoutDirection,
      a: Component[A],
      b: Component[B]
  ) extends Component[(A, B)]

  def runAndMakeUI: (Stream[Pure, A], ReactiveHtmlElement[SJSDiv]) =
    this match {
      case IntIR(label, bounds, initial) =>
        val currentValue = Var(initial.toString)
        val (min, max) = bounds.getOrElse(0, 100)
        val app = div(
          span(label),
          input(
            typ("range"),
            minAttr(min.toString),
            maxAttr(max.toString),
            defaultValue(initial.toString),
            onInput.mapToValue --> currentValue
          )
        )

        val values = Stream(initial).repeat.map(_ => currentValue.now().toInt)
        (values, app)

      case ColorIR(label, initColor) =>
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

        val currentValue = Var(colorToHex(initColor))
        val app = div(
          span(label),
          input(typ := "color", onInput.mapToValue --> currentValue),
          span(child.text <-- currentValue)
        )

        val values = Stream(initColor).repeat.map(_ => {
          // println(s"here: ${currentValue.now()}, ${hexToColor(currentValue.now())}")
          // println(hexToColor(currentValue.now()))
          hexToColor(currentValue.now())
        })
        (values, app)

      case BooleanIR(label, true) =>
        val currentValue = Var(false)
        val app = div(button(onClick.mapTo(true) --> currentValue, label))

        val values = Stream(false).repeat.map(_ => {
          val wasPressed = currentValue.now()
          currentValue.set(false)
          wasPressed
        })

        (values, app)

      case BooleanIR(labelText, false) =>
        val currentValue = Var(false)
        val app = div(
          label(labelText),
          input(typ := "checkbox", onChange.mapToChecked --> currentValue)
        )

        val values = Stream(false).repeat.map(_ => currentValue.now())
        (values, app)

      case ChoiceIR(label, choices, choiceLabels) =>
        val currentValue = Var(choiceLabels(0))
        val lastValidValue = Var(choiceLabels(0))
        val labelToChoice = choiceLabels.zip(choices).toMap
        val app = div(
          span(label),
          select(
            inContext { node =>
              onChange.mapTo(node.ref.value) --> currentValue.writer
            },
            choiceLabels.map(label => option(value := label, label))
          )
        )

        val values = Stream(choices(0)).repeat.map { _ =>
          val label = currentValue.now() match {
            case null => lastValidValue.now()
            case value => {
              lastValidValue.set(value)
              value
            }
          }

          labelToChoice(label)
        }

        (values, app)

      case LayoutIR(direction, a, b) =>
        val (aValues, aUI) = a.runAndMakeUI
        val (bValues, bUI) = b.runAndMakeUI

        val class_ = direction match {
          case LayoutDirection.Horizontal => "horizontal"
          case LayoutDirection.Vertical   => "vertical"
        }
        val ui = div(className := class_, aUI, bUI)
        val values = aValues.zip(bValues)
        (values, ui)
    }

  def run: Stream[Pure, A] = {
    val (values, ui) = runAndMakeUI
    val container = dom.document.querySelector("#explorer")

    render(container, ui)

    values
  }

  def run(containerId: String): Stream[Pure, A] = {
    val (values, ui) = runAndMakeUI
    val container = dom.document.querySelector(containerId)

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(container, ui)
    }(unsafeWindowOwner)

    values
  }
}

implicit object IntInterpreter extends ExploreInt[Component] {
  import Component.IntIR

  def int(label: String) = IntIR(label, None, 0)

  extension (generator: Component[Int])
    def within(start: Int, end: Int) =
      generator match {
        case generator: IntIR => generator.copy(bounds = Some(start, end))
      }

  extension (generator: Component[Int])
    def withDefault(initValue: Int) =
      generator match {
        case generator: IntIR => generator.copy(initial = initValue)
      }
}

implicit object ColorInterpreter extends ExploreColor[Component] {
  import Component.ColorIR

  def color(name: String) =
    ColorIR(name, Color.black.asInstanceOf[Color])

  extension (generator: Component[Color])
    def withDefault(initColor: Color) =
      generator match {
        case generator: ColorIR => generator.copy(initColor = initColor)
      }
}

implicit object BooleanInterpreter extends ExploreBoolean[Component] {
  import Component.BooleanIR

  override def boolean(label: String) = BooleanIR(label, false)
  override def asButton(generator: Component[Boolean]) = generator match {
    case generator: BooleanIR => generator.copy(isButton = true)
  }
  override def asCheckbox(generator: Component[Boolean]) = generator match {
    case generator: BooleanIR => generator.copy(isButton = false)
  }
}

implicit object ChoiceInterpreter extends ExploreChoice[Component] {
  import Component.ChoiceIR

  def choice[A](label: String, choices: Seq[A]) =
    ChoiceIR(label, choices.toChoices, choices.map(_.toString))
  def labeledChoice[A](label: String, choices: Seq[(String, A)]) =
    ChoiceIR(label, choices.map(_._2).toChoices, choices.map(_._1))
}

implicit object LayoutInterpreter extends Layout[Component] {
  import Component.LayoutIR

  extension [A, B](top: Component[A])
    def above(bottom: Component[B]) =
      LayoutIR(LayoutDirection.Vertical, top, bottom)

  extension [A, B](left: Component[A])
    def beside(right: Component[B]) =
      LayoutIR(LayoutDirection.Horizontal, left, right)
}
