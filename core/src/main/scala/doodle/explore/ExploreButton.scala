package doodle.explore

trait ExploreButton[F[_]] {
  def button(label: String): F[Boolean]
}
