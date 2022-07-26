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

enum Component[A] extends Explorer[A, Drawing, Algebra, Canvas, Frame] {
  case IntIR(label: String, bounds: Option[(Int, Int)], initial: Int)
      extends Component[Int]

  def run: Stream[Pure, A] = this match {
    case IntIR(label, bounds, initial) =>
      val currentValue = Var(initial.toString)
      documentEvents.onDomContentLoaded.foreach { _ =>
        val container = dom.document.querySelector("#container")
        val app = div(
          input(typ("range"), onInput.mapToValue --> currentValue),
          span(child.text <-- currentValue)
        )
        render(container, app)
      }(unsafeWindowOwner)

      Stream(0).repeat.map(_ => currentValue.now().toInt)
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
