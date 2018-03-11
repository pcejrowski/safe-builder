package safebuilder

import org.scalatest.{FlatSpec, Matchers}
import safebuilder.phantoms._

class phantomsTest extends FlatSpec with Matchers {

  behavior of "phantom types"

  they should "match to each other" in {
    implicitly[TTrue =:= TTrue]
    implicitly[TFalse =:= TFalse]
  }
  they should "flip the value" in {
    implicitly[Not[TTrue] =:= TFalse]
    implicitly[Not[TFalse] =:= TTrue]
  }
  they should "sum properly" in {
    implicitly[TFalse Or TFalse =:= TFalse]
    implicitly[TFalse Or TTrue =:= TTrue]
    implicitly[TTrue Or TFalse =:= TTrue]
    implicitly[TTrue Or TTrue =:= TTrue]
  }
  they should "intersect properly" in {
    implicitly[TFalse And TFalse =:= TFalse]
    implicitly[TFalse And TTrue =:= TFalse]
    implicitly[TTrue And TFalse =:= TFalse]
    implicitly[TTrue And TTrue =:= TTrue]
  }
  they should "apply mixed operations properly" in {
    implicitly[Not[TFalse And TTrue] =:= TTrue]
    implicitly[TTrue And Not[TTrue] =:= TFalse]
  }
  they should "not accept any bullshit" in {
    assertTypeError("implicitly[TTrue =:= TFalse]")
    assertTypeError("implicitly[Not[TTrue] =:= TTrue]")
    assertTypeError("implicitly[TFalse Or TTrue =:= TFalse]")
    assertTypeError("implicitly[TTrue And TFalse =:= TTrue]")
  }
}
