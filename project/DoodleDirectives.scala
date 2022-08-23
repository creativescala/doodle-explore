import cats.data._
import cats.implicits._
import laika.ast._
import laika.directive._

object DoodleDirectives extends DirectiveRegistry {

  override val description: String =
    "Directive to work with Doodle SVG pictures."

  val divWithId: Blocks.Directive =
    Blocks.create("divWithId") {
      import Blocks.dsl._

      attribute(0)
        .as[String]
        .map { (id) =>
          RawContent(
            NonEmptySet.one("html"),
            s"""<div id="${id}"></div>"""
          )
        }
    }

  val script: Blocks.Directive =
    Blocks.create("script") {
      import Blocks.dsl._

      (attribute(0).as[String]).map { (js) =>
        RawContent(NonEmptySet.one("html"), s"<script>$js</script>")
      }
    }

  // Parameters are id and then JS function to call
  val doodle: Blocks.Directive =
    Blocks.create("doodle") {
      import Blocks.dsl._

      (attribute(0).as[String], attribute(1).as[String], cursor)
        .mapN { (id, js, _) =>
          BlockSequence(
            Seq(
              RawContent(
                NonEmptySet.one("html"),
                s"""<div id="${id}"></div>"""
              ),
              RawContent(
                NonEmptySet.one("html"),
                s"""<script>${js}("${id}")</script>"""
              )
            )
          )
        }
    }

  val spanDirectives = Seq()
  val blockDirectives = Seq(divWithId, doodle, script)
  val templateDirectives = Seq()
  val linkDirectives = Seq()
}
