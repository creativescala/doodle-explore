package doodle.explore

trait ExploreBoolean[F[_]] {
  def boolean(label: String): F[Boolean]

  def asButton(generator: F[Boolean]): F[Boolean]
  def asCheckbox(generator: F[Boolean]): F[Boolean]

  def button(label: String) = asButton(boolean(label))
  def checkbox(label: String) = asCheckbox(boolean(label))
}
