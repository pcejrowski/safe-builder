package safebuilder

import org.scalatest.{FreeSpec, Matchers}
import safebuilder.phantoms._

class phantomsTest extends FreeSpec with Matchers {

  "phantom types" - {

    "match to each other" - {
      implicitly[TTrue =:= TTrue]
      implicitly[TFalse =:= TFalse]
    }
    "flip the value" - {
      implicitly[Not[TTrue] =:= TFalse]
      implicitly[Not[TFalse] =:= TTrue]
    }
    "sum properly" - {
      implicitly[TFalse Or TFalse =:= TFalse]
      implicitly[TFalse Or TTrue =:= TTrue]
      implicitly[TTrue Or TFalse =:= TTrue]
      implicitly[TTrue Or TTrue =:= TTrue]
    }
    "intersect properly" - {
      implicitly[TFalse And TFalse =:= TFalse]
      implicitly[TFalse And TTrue =:= TFalse]
      implicitly[TTrue And TFalse =:= TFalse]
      implicitly[TTrue And TTrue =:= TTrue]
    }
    "apply mixed operations properly" - {
      implicitly[Not[TFalse And TTrue] =:= TTrue]
      implicitly[TTrue And Not[TTrue] =:= TFalse]
    }
    "respect the boolean logic" - {
      assertTypeError("implicitly[TTrue =:= TFalse]")
      assertTypeError("implicitly[Not[TTrue] =:= TTrue]")
      assertTypeError("implicitly[TFalse Or TTrue =:= TFalse]")
      assertTypeError("implicitly[TTrue And TFalse =:= TTrue]")
    }
  }
}
