package doodle.explore

trait ExploreInt[F[_]] {
  def int(label: String): F[Int]
  def within(generator: F[Int], start: Int, end: Int): F[Int]
  def startingWith(generator: F[Int], initValue: Int): F[Int]
}

object IntComponentOps {
  extension [F[_]](component: F[Int])(using exploreInt: ExploreInt[F]) {
    def within(start: Int, end: Int) =
      exploreInt.within(component, start, end)

    def startingWith(initValue: Int) =
      exploreInt.startingWith(component, initValue)
  }
}
