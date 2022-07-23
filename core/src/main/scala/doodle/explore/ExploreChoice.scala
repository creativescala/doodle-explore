package doodle.explore

trait ExploreChoice[F[_]] {
  def choice[A](label: String, choices: Seq[A]): F[A]
  def labeledChoice[A](label: String, choices: Seq[(String, A)]): F[A]
  def labeledChoice[A](label: String, choices: Map[String, A]): F[A] =
    labeledChoice(label, choices.toSeq)
}
