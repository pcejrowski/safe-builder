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
libraryDependencies += "com.github.pcejrowski" %% "safe-builder" % "0.1.0"
```
or, if you are using Scala JS or Scala Native:
```
libraryDependencies += "com.github.pcejrowski" %%% "safe-builder" % "0.1.0"
```
Finally, enable [Macro Paradise](https://docs.scala-lang.org/overviews/macros/paradise.html):
```
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.full)
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
Following functions are generated depending on the type of the field:

|  Field name  |      Type     |     Functions     |
|:------------:|:-------------:|:-----------------:|
| `foo`        | `A`           | `withFoo(foo: A)` |
| `foo`        | `Option[A]`   | `withFoo(foo: A)` |
| `foo`        | `Either[A,B]` | `withFoo(foo: A)` |
|              |               | `withFoo(foo: B)` |
| `foo`        | `Boolean`     | `fooEnabled`      |
|              |               | `fooDisabled`     |

## Contributing

#### Running tests
Tests are configured to run on all platforms (JVM, JS, Native). Simply invoke `sbt "+test"`

#### Releasing artifacts
`safe-builder` uses [https://github.com/sbt/sbt-release](sbt-release). Simply invoke `release` from the root project to release all artifacts.