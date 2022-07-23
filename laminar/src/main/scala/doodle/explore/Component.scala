package doodle.explore.laminar

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import fs2.Stream
import fs2.Pure

import doodle.explore.Explorer
import doodle.explore.ExploreInt

enum Component[A] extends Explorer[A] {
  case IntIR(label: String, bounds: Option[(Int, Int)], initial: Int)
      extends Component[Int]

  def run: Stream[Pure, A] = this match {
    case IntIR(label, bounds, initial) =>
      documentEvents.onDomContentLoaded.foreach { _ =>
        val container = dom.document.querySelector("#container")
        val app = div("test")
        render(container, app)
      }(unsafeWindowOwner)
      Stream(0).repeat
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
