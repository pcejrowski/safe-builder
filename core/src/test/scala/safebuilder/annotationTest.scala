package safebuilder

import org.scalatest.{FlatSpec, Matchers}
import safebuilder.annotation.builder

class annotationTest extends FlatSpec with Matchers {

  behavior of "@builder"
  it should "build if all params are there" in {
    @builder case class Foo(foo: Option[String], bar: Boolean, baz: Either[Float, String])
    val testee = Foo()
      .withFoo("foo-string")
      .barEnabled
      .withBaz(.3f)
      .build
    testee.foo should be(Some("foo-string"))
    testee.bar should be(true)
    testee.baz should be(Left(.3f))
  }

  it should "build if all non-optional params are there" in {
    @builder case class Foo(foo: Option[String], bar: Boolean, baz: Either[Float, String])
    val testee = Foo()
      .barEnabled
      .withBaz(.3f)
      .build
    testee.foo should be(None)
    testee.bar should be(true)
    testee.baz should be(Left(.3f))
  }

  it should "respect default value" in {
    @builder case class Foo(foo: String = "foo")
    val testee = Foo().build
    testee.foo should be("foo")
  }

  it should "allow to overwrite default value" in {
    @builder case class Foo(foo: String = "foo")
    val testee = Foo()
      .withFoo("diff-foo")
      .build
    testee.foo should be("diff-foo")
  }

  it should "not build if not all params are there" in {
    @builder case class Foo(foo: Option[String], bar: Boolean, baz: Either[Float, String])
    assertTypeError("""Foo().withFoo("foo").build""")
    assertTypeError("""Foo().withBar(7).build""")
    assertTypeError("""Foo().withBaz(.3f).build""")
    assertTypeError("""Foo().withFoo("foo").withBar(7).build""")
    assertTypeError("""Foo().withFoo("foo").withBaz(.3f).build""")
    assertTypeError("""Foo().withBar(7).withBaz(.3f).build""")
  }

  it should "allow annotating more case classes" in {
    @builder case class Foo(foo: Option[String])
    @builder case class Bar(bar: Boolean)

    val testee1 = Foo().withFoo("foo-string").build
    testee1.foo should be(Some("foo-string"))

    val testee2 = Bar().barEnabled.build
    testee2.bar should be(true)
  }

  it should "fail on a case class without parameters" in {
    assertTypeError("""@builder case class Foo()""")
  }

  it should "fail on not a case class" in {
    assertTypeError("""@builder object Foo""")
    assertTypeError("""@builder class Foo""")
    assertTypeError("""@builder trait Foo""")
  }

}
