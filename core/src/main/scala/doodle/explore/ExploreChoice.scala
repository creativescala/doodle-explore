package doodle.explore

import scala.language.implicitConversions

case class Choice[A](value: A)

object ChoiceConversions {
  implicit def choiceToValue[A](choice: Choice[A]): A = choice.value
  implicit def valueToChoice[A](value: A): Choice[A] = Choice(value)
  implicit def valueSeqToChoiceSeq[A](values: Seq[A]): Seq[Choice[A]] = values.map(Choice(_))
}

trait ExploreChoice[F[_]] {
  def choice[A](label: String, choices: Seq[A]): F[Choice[A]]
  def labeledChoice[A](label: String, choices: Seq[(String, A)]): F[Choice[A]]
  def labeledChoice[A](label: String, choices: Map[String, A]): F[Choice[A]] =
    labeledChoice(label, choices.toSeq)
}
