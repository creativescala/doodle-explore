package doodle.explore.laminar

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import fs2.Stream
import fs2.Pure

import doodle.explore.Explorer
import doodle.explore.ExploreInt

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

enum Component[A] extends Explorer[A, Drawing, Algebra, Canvas, Frame] {
  case IntIR(label: String, bounds: Option[(Int, Int)], initial: Int)
      extends Component[Int]
  case ColorIR(label: String, initColor: Color) extends Component[Color]
  case LayoutIR[A, B](direction: LayoutDirection, a: Component[A], b: Component[B])
    extends Component[(A, B)]

  def runAndMakeUI: (Stream[Pure, A], ReactiveHtmlElement[SJSDiv]) = this match {
    case IntIR(label, bounds, initial) =>
      val currentValue = Var(initial.toString)
      val app = div(
        span(label),
        input(typ("range"), onInput.mapToValue --> currentValue),
        span(child.text <-- currentValue)
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
            case x => x
          }
          res
        }

        s match {
          case rgb(rStr, gStr, bStr) =>
            val Array(r, g, b) = Array(rStr, gStr, bStr).map(hexToBytes).map(_.last)
            Color.rgb(r, g, b)

          case rgba(rStr, gStr, bStr, aStr) =>
            val Array(r, g, b, a) = Array(rStr, gStr, bStr, aStr).map(hexToBytes).map(_.last)
            Color.rgba(r, g, b, a.toDouble / 255.0)
        }
      }

      val currentValue = Var(colorToHex(initColor))
      val app = div(
        span(label),
        input(typ("color"), onInput.mapToValue --> currentValue),
        span(child.text <-- currentValue),
      )

      val values = Stream(initColor).repeat.map(_ => {
        // println(s"here: ${currentValue.now()}, ${hexToColor(currentValue.now())}")
        // println(hexToColor(currentValue.now()))
        hexToColor(currentValue.now())
      })
      (values, app)

    case LayoutIR(direction, a, b) =>
      val (aValues, aUI) = a.runAndMakeUI
      val (bValues, bUI) = b.runAndMakeUI

      val class_ = direction match {
        case LayoutDirection.Horizontal => "horizontal"
        case LayoutDirection.Vertical => "vertical"
      }
      val ui = div(className := class_, aUI, bUI)
      val values = aValues.zip(bValues)
      (values, ui)
  }

  def run: Stream[Pure, A] = {
    val (values, ui) = runAndMakeUI
    val container = dom.document.querySelector("#container")

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(container, ui)
    }(unsafeWindowOwner)

    values
  }
}

implicit object IntInterpreter extends ExploreInt[Component] {
  import Component.IntIR

  override def int(label: String) = IntIR(label, None, 0)

  override def within(generator: Component[Int], start: Int, end: Int) =
    generator

  override def startingWith(generator: Component[Int], initValue: Int) =
    generator
}

implicit object ColorInterpreter extends ExploreColor[Component] {
  import Component.ColorIR

  override def color(name: String) =
    ColorIR(name, Color.black.asInstanceOf[Color])

  override def withDefault(generator: Component[Color], initColor: Color) =
    generator match {
      case generator: ColorIR         => generator.copy(initColor = initColor)
    }
}

implicit object LayoutInterpreter extends Layout[Component] {
  import Component.LayoutIR

  def above[A, B](top: Component[A], bottom: Component[B]) = LayoutIR(LayoutDirection.Vertical, top, bottom)
  def beside[A, B](left: Component[A], right: Component[B]) = LayoutIR(LayoutDirection.Horizontal, left, right)
}
