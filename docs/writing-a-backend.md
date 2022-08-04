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

        frame.add(ui)
        frame.setVisible(true)
        frame.pack
        values
    }
}
```
