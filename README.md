# safe-builder

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Scala micro-library for generating type-safe builder for any `case class` via annotation.

## Installation
Add resolver to bintray repo:
```
resolvers += "pcejrowski maven" at "https://dl.bintray.com/pcejrowski/maven"
```
and add the following dependency to your `build.sbt`.
```
libraryDependencies += "com.github.pcejrowski" %% "safe-builder" % "0.1.2"
```
or, if you are using Scala JS or Scala Native:
```
libraryDependencies += "com.github.pcejrowski" %%% "safe-builder" % "0.1.2"
```
Finally, enable [Macro Paradise](https://docs.scala-lang.org/overviews/macros/paradise.html):
```
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
```

## Usage
Annotate your class with `@builder`
```scala
import safebuilder.annotation.builder

@builder
case class Foo(name: Option[String], bar: Boolean, baz: Either[Float, String])
```
and enjoy builder API with compile-time correctness verification.
```scala
val foo = Foo()
  .withName("my-string")
  .barEnabled
  .withBaz(.3f)
  .build
``` 
The library will make sure that all non-optional fields are filled when you invoke `build`. Moreover, no field value can be overwritten.
That means, for already defined class `Foo` following cases will not compile:
```scala
Foo().withName("my-string).barEnabled.build
```
```scala
Foo().withBaz(.3f).build
```
because required parameters (`baz` and `bar`, respectively) are not defined.

Following functions are generated depending on the type of the field:

|  Field name  |      Type     |     Functions     |
|:------------:|:-------------:|:-----------------:|
| `foo`        | `A`           | `withFoo(foo: A)` |
| `foo`        | `Option[A]`   | `withFoo(foo: A)` |
| `foo`        | `Either[A,B]` | `withFoo(foo: A)` |
|              |               | `withFoo(foo: B)` |
| `foo`        | `Boolean`     | `fooEnabled`      |
|              |               | `fooDisabled`     |

#### Defaults
If there is a default (rhs) value defined for a field (e.g. `case class Foo(foo: String = "foo")`,
the invocation of mutator for that field is no longer required and the default value will be used.
Since the default value for `Option` is `None` by default, this setup will also accept to use `Some[_]`
as a default value, even though it's considered to be an ambiguous anti-pattern.

#### Debugging
Add `-Ymacro-debug-lite` flag to `scalac` options and generated source code will show up in the console when you compile.
```
scalacOptions in Compile += "-Ymacro-debug-lite"
```

## Contributing

#### Running tests
Tests are configured to run on all platforms (JVM, JS, Native). Simply invoke `sbt "+test"`

#### Releasing artifacts
`safe-builder` uses [https://github.com/sbt/sbt-release](sbt-release). Simply invoke `release` from the root project to release all artifacts.