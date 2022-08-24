package doodle.explore

/** Base module for constructors
  *
  * Most users will only use a single backend. Abstracting over backends is
  * useful for Doodle developers but not so useful for the typical user.
  *
  * To improve the experience for a typical user we provide mixins that require
  * the below interface, and construct a concrete implementation for each
  * backend. This allows users to access constructors without having to specify
  * given instances, reducing the complexity of using the library.
  */
trait BaseConstructor {

  /** The type of all algebras a given concrete implementation implements. */
  type Algebra[F[_]]

  /** The type of UI components that a given concrete implementation produces.
    */
  type Component[A]

  /** The given instance implementing all the algebras. */
  given algebra: Algebra[Component]

}
