package doodle
package explore
package js

import doodle.core._
import doodle.image._
import doodle.image.syntax._
import doodle.image.syntax.all._
import doodle.image.syntax.core._

import doodle.java2d.effect.Center._
import doodle.java2d.effect.Redraw._
import doodle.java2d.effect.Size._

import doodle.syntax.all.{RendererFrameOps, AngleIntOps}
import doodle.interact.syntax._

// import doodle.svg._

import doodle.explore.{Explorer, ExploreInt}
import doodle.explore.IntComponentOps._

import cats.effect.unsafe.implicits.global

import fs2.{Stream, Pure}

import slinky.core._
import slinky.web.ReactDOM
import slinky.hot

import org.scalajs.dom

enum JSComponent[A] extends Explorer[Unit, A] {
  case IntIR(n: Int) extends JSComponent[Int]

  def run: Stream[Pure, A] = this match {
    case IntIR(n) => Stream(n).repeat
  }
}

implicit object IntInterpreter extends ExploreInt[JSComponent] {
  import JSComponent.IntIR

  override def int(label: String) = IntIR(0)
  override def within(generator: JSComponent[Int], start: Int, end: Int) = IntIR(0)
  override def startingWith(generator: JSComponent[Int], newInitial: Int) = IntIR(newInitial)
}

object SlinkyStuff {
  import slinky.core._
  import slinky.core.annotations.react
  import slinky.web.html._

  import scala.scalajs.js
  import scala.scalajs.js.annotation.JSImport

  @react class App extends StatelessComponent {
    type Props = Unit

    def render() = {
      div("test")
    }
  }
}

object Main {
  def explorer(using 
    intGui: ExploreInt[JSComponent], 
    // colorGui: ExploreColor[Component],
    // layout: Layout[Component]
    ) = {
      import intGui._
      // import colorGui._
      // import layout._

      int("Base Size").within(1 to 60).startingWith(10)
      // (int("Base Size") within (1 to 60) startingWith 10)
      //   .above(int("Iterations") within (1 to 5) startingWith 1)
      //   .above(int("Stroke Width") within (1 to 20) startingWith 2)
      //   .above(color("Background") withDefault Color.white)
      //   .above(color("Foreground") withDefault Color.black)
      //   .above(
      //     (int("X Offset") within (-1000 to 1000))
      //       .beside(int("Y Offset") within (-1000 to 1000))
      //   )
  }


  @JSExportTopLevel("main")
  def main(args: Array[String]) = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }

    ReactDOM.render(App(), container)

    // explorer.run
    // println("Hello world")
  }
}
