package doodle.explore

import cats.effect.IOApp.Simple

trait GUI[F[_]] {
  def sliderInt(name: String, start: Int, end: Int): F[Int]
}

trait Layout[F[_]] {
  def above[A, B](topComponent: F[A], bottomComponent: F[B]): F[(A, B)]
}

case class Component[A](val values: LazyList[A])

implicit object GUIInterpreter extends GUI[Component] {
  override def sliderInt(name: String, start: Int, end: Int): Component[Int] = {
    ???
  }
}

implicit object LayoutInterpreter extends Layout[Component] {
  override def above[A, B](topComponent: Component[A], bottomComponent: Component[B]): Component[(A, B)] = {
    Component(topComponent.values.zip(bottomComponent.values))
  }
}

def prog(implicit gui: GUI[Component], layout: Layout[Component]) = {
  import gui._
  import layout._

  above(sliderInt("Line Width", 0, 10), sliderInt("Iterations", 2, 10))
}
