/*
 * Copyright 2022 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doodle.explore

case class Choice[A](value: A)

trait ExploreChoice[Component[_]] {
  def choice[A](label: String, choices: Seq[A]): Component[Choice[A]]
  def labeledChoice[A](
      label: String,
      choices: Seq[(String, A)]
  ): Component[Choice[A]]
  def labeledChoice[A](
      label: String,
      choices: Map[String, A]
  ): Component[Choice[A]] =
    labeledChoice(label, choices.toSeq)
}

trait ExploreChoiceConstructor[Component[_]](
    algebra: ExploreChoice[Component]
) {
  def choice[A](label: String, choices: Seq[A]): Component[Choice[A]] =
    algebra.choice(label, choices)

  def labeledChoice[A](
      label: String,
      choices: Seq[(String, A)]
  ): Component[Choice[A]] =
    algebra.labeledChoice(label, choices)

  def labeledChoice[A](
      label: String,
      choices: Map[String, A]
  ): Component[Choice[A]] =
    labeledChoice(label, choices.toSeq)
}
