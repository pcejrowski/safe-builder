package safebuilder

import org.scalatest.{FreeSpec, Matchers}
import safebuilder.annotation.builder

class annotationTest extends FreeSpec with Matchers {

  "@builder" - {
    "doesn't affect existing functionality" - {
      "built-in apply works" in {
        @builder case class Foo(foo: String)
        Foo("foo").foo shouldBe "foo"
      }
    }

    "has support for type:" - {
      "Option" in {
        @builder case class Foo(foo: Option[String], bar: Option[Int], baz: Option[Boolean])
        val testee = Foo()
          .withFoo("foo-string")
          .withBaz(false)
          .build
        testee.foo shouldBe Some("foo-string")
        testee.bar shouldBe None
        testee.baz shouldBe Some(false)
      }
      "Either" in {
        @builder case class Foo(foo: Either[Float, String], bar: Either[Float, String])
        val testee = Foo()
          .withFoo(.3f)
          .withBar("bar-string")
          .build
        testee.foo shouldBe Left(.3f)
        testee.bar shouldBe Right("bar-string")
      }
      "Boolean" in {
        @builder case class Foo(foo: Boolean, bar: Boolean)
        val testee = Foo()
          .fooEnabled
          .barDisabled
          .build
        testee.foo shouldBe true
        testee.bar shouldBe false
      }
      "Any (...other)" in {
        case class Baz(baz: String)
        @builder case class Foo(foo: String, bar: Float, baz: Baz)
        val testee = Foo()
          .withFoo("foo")
          .withBar(.3f)
          .withBaz(Baz("baz"))
          .build
        testee.foo shouldBe "foo"
        testee.bar shouldBe .3f
        testee.baz shouldBe Baz("baz")
      }
    }
    "respects default values" - {
      "doesn't require to specify it" in {
        @builder case class Foo(foo: String = "foo")
        val testee = Foo().build
        testee.foo shouldBe "foo"
      }
      "allows to overwrite it" in {
        @builder case class Foo(foo: String = "foo")
        val testee = Foo()
          .withFoo("diff-foo")
          .build
        testee.foo shouldBe "diff-foo"
      }
    }
    "allow annotating more case classes" - {
      @builder case class Foo(foo: Option[String])
      @builder case class Bar(bar: Boolean)

      val testee1 = Foo().withFoo("foo-string").build
      testee1.foo shouldBe Some("foo-string")

      val testee2 = Bar().barEnabled.build
      testee2.bar shouldBe true
    }
    "fails if applied badly" - {
      "on not a case class" in {
        assertTypeError("""@builder object Foo""")
        assertTypeError("""@builder class Foo""")
        assertTypeError("""@builder trait Foo""")
      }
      "on a case class without parameters" in {
        assertTypeError("""@builder case class Foo()""")
      }
    }
    "not build if not all parameters are defined" - {
      @builder case class Foo(foo: Option[String], bar: Boolean, baz: Either[Float, String])
      assertTypeError("""Foo().withFoo("foo").build""")
      assertTypeError("""Foo().withBar(7).build""")
      assertTypeError("""Foo().withBaz(.3f).build""")
      assertTypeError("""Foo().withFoo("foo").withBar(7).build""")
      assertTypeError("""Foo().withFoo("foo").withBaz(.3f).build""")
      assertTypeError("""Foo().withBar(7).withBaz(.3f).build""")
    }
  }
}
