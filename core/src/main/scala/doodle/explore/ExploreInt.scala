package doodle.explore

trait ExploreInt[F[_], S <: F[Int]] {
  def int(label: String): F[Int]
  def within(generator: F[Int], start: Int, end: Int): S
  def startingWith(generator: S, initValue: Int): S
}

implicit class IntComponentOps[F[_], S <: F[Int]](component: F[Int])(implicit exploreInt: ExploreInt[F, S]) {
  def within(start: Int, end: Int) = {
    exploreInt.within(component, start, end)
  }
}

implicit class IntSliderOps[F[_], S <: F[Int]](component: S)(implicit exploreInt: ExploreInt[F, S]) {
  def startingWith(initValue: Int) = {
    exploreInt.startingWith(component, initValue)
  }
}
