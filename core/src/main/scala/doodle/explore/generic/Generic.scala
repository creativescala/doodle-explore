package doodle.explore.generic

import doodle.core.Color
import doodle.explore.*

sealed abstract class BaseComponent[A] extends Product with Serializable

final case class IntComponent(
    label: String,
    range: Option[(Int, Int)],
    default: Int
) extends BaseComponent[Int] {
  def within(start: Int, stop: Int): IntComponent =
    this.copy(range = Some((start, stop)))

  def withDefault(default: Int): IntComponent =
    this.copy(default = default)
}
object IntComponent {
  given exploreInt: ExploreInt[BaseComponent, IntComponent] with {
    def int(label: String): IntComponent = IntComponent(label, None, 0)

    extension (generator: IntComponent)
      def within(start: Int, end: Int): IntComponent =
        generator.within(start, end)

    extension (generator: IntComponent)
      def withDefault(default: Int): IntComponent =
        generator.withDefault(default)
  }
}

final case class ColorComponent(label: String, default: Color)
    extends BaseComponent[Color] {
  def withDefault(default: Color): ColorComponent =
    this.copy(default = default)
}
object ColorComponent {
  given exploreColor: ExploreColor[BaseComponent, ColorComponent] with {
    def color(label: String): ColorComponent =
      ColorComponent(label, Color.black)

    extension (generator: ColorComponent)
      def withDefault(default: Color): ColorComponent =
        generator.withDefault(default)
  }
}
