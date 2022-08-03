package doodle.explore

import scala.language.implicitConversions

case class Choice[A](value: A)

object ChoiceOps {
  extension [A](values: Seq[A]) {
    def toChoices: Seq[Choice[A]] = values.map(Choice(_))
  }
}

trait ExploreChoice[F[_]] {
  def choice[A](label: String, choices: Seq[A]): F[Choice[A]]
  def labeledChoice[A](label: String, choices: Seq[(String, A)]): F[Choice[A]]
  def labeledChoice[A](label: String, choices: Map[String, A]): F[Choice[A]] =
    labeledChoice(label, choices.toSeq)
}
