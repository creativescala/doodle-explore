# Explorer Types

Doodle Explore provides several `Explore*` traits with implementations
for the Java2D and Laminar backends. Example usage of all of these
traits can be found in the `*Explorer` functions in `doodle.explore.laminar`. 

## ExploreInt

`ExploreInt` describes a DSL for adding an integer input to your GUI.
It's the simplest explorer type and a good start point for understanding
the library. Like most explorer types, it has an `IntComponentOps` module
which provides some helpful extension methods.

## ExploreColor

`ExploreColor` is another simple type of input. Implementing it for a specific
backend will require converting to and from the backend's color type to `doodle.core.Color`.

## ExploreBoolean

`ExploreBoolean` provides a DSL for boolean inputs. It differs from `ExploreInt`
in that it provides methods to construct the input component as either a button
or as a checkbox. In button mode, a component will emit `true` for only the frame 
that it is clicked, which is useful for simulations that make use of `exploreWithState`,
but not for still images.

## ExploreChoice

`ExploreChoice` represents a component allowing a user to choose options from a list.
Because it is generic over the type of option, it uses a wrapper `Choice` type to prevent
using methods from other `Explore*` DSLs. For choice types with a useful `toString` implementation,
the default `choice(label, choices)` function will work. However, it may be better to use
`labeledChoice` instead in order to explicitly label your choices.
