package doodle.explore

/** Describes a DSL for exploring an integer value. These functions can be used
  * with dot or infix notation through [[IntComponentOps]].
  */
trait ExploreInt[F[_]] {
  def int(label: String): F[Int]
  extension (generator: F[Int]) def within(start: Int, end: Int): F[Int]
  extension (generator: F[Int]) def withDefault(initValue: Int): F[Int]

  extension (generator: F[Int])
    def within(range: Range): F[Int] =
      if (range.isInclusive) generator.within(range.start, range.end)
      else generator.within(range.start, range.end - 1)
}
