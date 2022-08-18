package doodle.explore

/** Describes a DSL for setting the layout of a given [[Explorer]] component.
  * These functions can be used with dot or infix notation through
  * [[LayoutOps]].
  */
trait Layout[F[_]] {
  extension [A, B](top: F[A]) def above(bottom: F[B]): F[(A, B)]
  extension [A, B](left: F[A]) def beside(right: F[B]): F[(A, B)]
}
