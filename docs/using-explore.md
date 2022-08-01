# Making and Explorer for your Art Piece

Using Explore, creating a GUI for your doodle art means writing
two functions: `explorer`, and `render`. `explorer` describes the
GUI of the doodle, and `render` generates a `Picture` from the GUI's
output values.

## Explorer

Your `explorer` function describes the GUI for your doodle, implicitly using
a few `explore` interpreters targeting your preferred backend. You'll need
an implicit argument for each type of component, e.g. `ExploreInt` or `ExploreColor`.
You'll probably also want to use a `Layout` interpreter. Interpreters are generic
over their `Component` type, which corresponds to the targeted backend. The two
backends built in to Doodle Explore are `doodle.explore.java2d.Component`, targeting
Java Swing, and `doodle.explore.laminar.Component`, targeting HTML/JS via `laminar`.

Let's make a simple GUI for the Sierpinski triangle using `explore`. It should allow
users to adjust the base triangle size, the number of iterations, and the stroke color
of the triangles. To accomplish this, we'll need the interpreters `ExploreInt` and `ExploreColor`
for our components, and then a `Layout` interpreter to put them together. We'll start with the
`java2d` backend to avoid dealing with HTML. Our function skeleton looks like this:

```scala
import doodle.explore.java2d.Component
import doodle.explore.{ExploreInt, ExploreColor, Layout}
import doodle.explore.syntax.all._

def explorer(using
    intGui: ExploreInt[Component],
    colorGui: ExploreColor[Component],
    layoutGui: Layout[Component],
) = {
    ???
}
```

Now let's add some components. To add a GUI component that outputs an
integer, we can use any function found in `ExploreInt` that returns a `F[Int]`.
To start out with, use `int(label: String)`. This element will adjust the
base size of the triangles. We probably want to restrict this to a range,
so we can use `within`. `IntComponentOps` contains a few extension methods
so we can call it in infix.

```scala
int("Base Size").within(1 to 30)
```

To compose multiple components and choose how they are layed out, we use
`Layout`. Let's add another integer to adjust the number of iterations:

```scala
int("Base Size").within(1 to 30) 
    .beside(int("Iterations").within(1 to 10).startingWith(2))
```

Finally, we'll use `ExploreColor` to add a color picker. Our final function
looks like this:
```scala
def explorer(using
    intGui: ExploreInt[Component],
    colorGui: ExploreColor[Component],
    layoutGui: Layout[Component],
) = {
    int("Base Size").within(1 to 30) 
        .beside(int("Iterations").within(1 to 10).startingWith(2))
        .above(color("Stroke Color"))
}
```

## Render

The `render` function describes how to create a `Picture` from the values
produced by your `explorer`.
