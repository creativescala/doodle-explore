# Writing an Explore Backend

Explore backends are implemented as an interpreter for each supported input type.
Each interpreter implements an `Explore*` trait for a backend-specific component type.
Along with the interpreters for each supported input type, there should be an interpreter for 
the `Layout` trait, so that multiple components can be composed. Additionally, the component type
must extend the `Explorer` trait parameterized by the targeted doodle backend's `Drawing`, `Algebra`,
`Canvas`, and `Frame` type. The reference backend implementation is `doodle.explore.java2d.Component.scala`.

## A minimal Java2D backend

Let's write a minimal Java2D backend with an implementation for `IntExplore` and `Layout`.
We'll start with our `Component` type. It is easiest expressed as a Scala 3 enum with a
case for each type of input. We'll use `doodle.java2d.{Drawing, Algebra, Canvas, Frame}`
as our Doodle backend.

```scala
enum Component[A] extends Explorer[A, Drawing, Algebra, Canvas, Frame] {
    ...

    def run: Stream[Pure, A] = ??? // required by the Explorer trait
}
```

### Implementing ExploreInt

To figure out the representation of our int subtype, we can look at what's
needed to implement `ExploreInt`.

```scala
enum Component[A] extends Explorer[A, Drawing, Algebra, Canvas, Frame] {
    case IntIR(label: String, bounds: (Int, Int), initial: Int) extends Component[Int]

    def run: Stream[Pure, A] = {
        val frame = JFrame("Explorer")

        val (ui, values) = this match {
            case IntIR(labelText, (start, end), initial) =>
                val panel = JPanel()
                val label = JLabel(labelText)
                val slider = JSlider(start, end, initial)

                panel.add(label, panel.add(slider))
                val values = Stream(initial).repeat.map(_ => slider.getValue)

                (panel, values)
        }

        // displaying the UI
        frame.add(ui)
        frame.setVisible(true)
        frame.pack

        // returning the values stream
        values
    }
}
```

Next, we have to write the actual `ExploreInt` implementation, using
`Component` as the internal type:

```scala
implicit object IntInterpreter extends ExploreInt[Component] {
  import Component.IntIR

  // Construct a brand new IntIR
  override def int(label: String) = IntIR(label, None, 0)

  // Pattern match to retrieve `generator` as the IntIR subtype, and then
  // update it.
  override def within(generator: Component[Int], start: Int, end: Int) =
    generator match {
      case generator: IntIR =>
        generator.copy(bounds = Some(start, end), initial = (start + end) / 2)
    }

  override def startingWith(generator: Component[Int], newInitial: Int) =
    generator match {
      case generator: IntIR => 
        generator.copy(initial = newInitial)
    }
}
```

This implementation will work for a single int slider, but we can't
compose them until there's an implementation for `Layout`.

### Implementing Layout

Implementing Layout with Java2D requires a slight restructuring. We want
our whole GUI to fit in the same window, so we can only create one JFrame.
The easiest way to solve this is to extract the UI creation logic into a
separate function and let `run` handle creating the JFrame after the whole
UI JComponent has been constructed. We'll call the new function `runAndMakeUI`.
Now our `Component` looks like this:

```scala
enum Component[A] extends Explorer[A, Drawing, Algebra, Canvas, Frame] {
    case IntIR(label: String, bounds: (Int, Int), initial: Int) extends Component[Int]

    def runAndMakeUI: (JComponent, Stream[Pure, A]) = this match {
        case IntIR(labelText, (start, end), initial) =>
            val panel = JPanel()
            val label = JLabel(labelText)
            val slider = JSlider(start, end, initial)

            panel.add(label, panel.add(slider))
            val values = Stream(initial).repeat.map(_ => slider.getValue)

            (panel, values)
    }

    def run: Stream[Pure, A] = {
        val frame = JFrame("Explorer")

        val (ui, values) = runAndMakeUI

        // displaying the UI
        frame.add(ui)
        frame.setVisible(true)
        frame.pack

        // returning the values stream
        values
    }
}
```

Next, we can add a `LayoutIR` subtype to our `Component`. It may use `Explore`'s
`LayoutDirection` type for consistency.

```scala
    case LayoutIR[A, B](direction: LayoutDirection, a: Component[A], b: Component[B]) extends Component[(A, B)]
```

Adding it to `runAndMakeUI`, we get:
```scala
def runAndMakeUI: (JComponent, Stream[Pure, A]) = this match {
    case IntIR(labelText, (start, end), initial) =>
        ...

    case LayoutIR(direction, a, b) =>
      val (aUI, aValues) = a.runAndMakeUI
      val (bUI, bValues) = b.runAndMakeUI

      val directionInt = direction match {
        case LayoutDirection.Horizontal => BoxLayout.X_AXIS
        case LayoutDirection.Vertical   => BoxLayout.Y_AXIS,
      }

      val panel = JPanel()
      panel.setLayout(BoxLayout(panel, direction))
      panel.add(a)
      panel.add(b)

      val zippedValues = aValues.zip(bValues)
      (panel, zippedValues)
}
```

Next, the `explore.Layout` implementation:
```scala
implicit object LayoutInterpreter extends Layout[Component] {
  import Component.LayoutIR

  def above[A, B](top: Component[A], bottom: Component[B]) =
    LayoutIR(LayoutDirection.Vertical, top, bottom)

  def beside[A, B](left: Component[A], right: Component[B]) =
    LayoutIR(LayoutDirection.Horizontal, left, right)
}
```

Our final code looks like this:

```scala
enum Component[A] extends Explorer[A, Drawing, Algebra, Canvas, Frame] {
    case IntIR(label: String, bounds: (Int, Int), initial: Int) extends Component[Int]
    case LayoutIR[A, B](direction: LayoutDirection, a: Component[A], b: Component[B]) extends Component[(A, B)]

    def runAndMakeUI: (JComponent, Stream[Pure, A]) = this match {
        case IntIR(labelText, (start, end), initial) =>
            val panel = JPanel()
            val label = JLabel(labelText)
            val slider = JSlider(start, end, initial)

            panel.add(label, panel.add(slider))
            val values = Stream(initial).repeat.map(_ => slider.getValue)

            (panel, values)

        case LayoutIR(direction, a, b) =>
          val (aUI, aValues) = a.runAndMakeUI
          val (bUI, bValues) = b.runAndMakeUI

          val directionInt = direction match {
            case LayoutDirection.Horizontal => BoxLayout.X_AXIS
            case LayoutDirection.Vertical   => BoxLayout.Y_AXIS,
          }

          val panel = JPanel()
          panel.setLayout(BoxLayout(panel, direction))
          panel.add(a)
          panel.add(b)

          val zippedValues = aValues.zip(bValues)
          (panel, zippedValues)
    }

    def run: Stream[Pure, A] = {
        val frame = JFrame("Explorer")

        val (ui, values) = runAndMakeUI

        // displaying the UI
        frame.add(ui)
        frame.setVisible(true)
        frame.pack

        // returning the values stream
        values
    }
}

implicit object IntInterpreter extends ExploreInt[Component] {
  import Component.IntIR

  // Construct a brand new IntIR
  override def int(label: String) = IntIR(label, None, 0)

  // Pattern match to retrieve `generator` as the IntIR subtype, and then
  // update it.
  override def within(generator: Component[Int], start: Int, end: Int) =
    generator match {
      case generator: IntIR =>
        generator.copy(bounds = Some(start, end), initial = (start + end) / 2)
    }

  override def startingWith(generator: Component[Int], newInitial: Int) =
    generator match {
      case generator: IntIR => 
        generator.copy(initial = newInitial)
    }
}

implicit object LayoutInterpreter extends Layout[Component] {
  import Component.LayoutIR

  def above[A, B](top: Component[A], bottom: Component[B]) =
    LayoutIR(LayoutDirection.Vertical, top, bottom)

  def beside[A, B](left: Component[A], right: Component[B]) =
    LayoutIR(LayoutDirection.Horizontal, left, right)
}
```
