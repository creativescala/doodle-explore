package doodle.explore

trait Layout[F[_]] {
  def above[A, B](top: F[A], bottom: F[B]): F[(A, B)]
  def beside[A, B](left: F[A], right: F[B]): F[(A, B)]
}

object LayoutOps {
  extension [F[_], A](component: F[A])(using layout: Layout[F]) {
    def above[B](other: F[B]) = layout.above(component, other)
    def beside[B](other: F[B]) = layout.beside(component, other)

    def ===[B](other: F[B]) = component.above(other)
    def |[B](other: F[B]) = component.beside(other)
  }
}
