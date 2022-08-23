# Doodle Explore

Doodle Explore is a library to quickly create user interfaces that control [Doodle](https://github.com/creativescala/doodle) images. For example, you can use Doodle Explore to explore how changing parameters affects a visualization of a complex system or find the most aesthetically pleasing settings for a generative art picture.

## Using 

This library is currently available for Scala version 3.1.

To use the latest version, include the following in your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.creativescala" %% "doodle-explore" % "@VERSION@"
)
```

@:divWithId(explorer)
@:doodle(svg, Example.go)
