package safebuilder

object phantoms {

  sealed trait TBool {
    type And[B <: TBool] <: TBool
    type Or[B <: TBool] <: TBool
    type Not <: TBool
  }

  sealed trait TTrue extends TBool {
    type And[B <: TBool] = B
    type Or[B <: TBool] = TTrue
    type Not = TFalse
  }

  sealed trait TFalse extends TBool {
    type And[B <: TBool] = TFalse
    type Or[B <: TBool] = B
    type Not = TTrue
  }

  type And[A <: TBool, B <: TBool] = A#And[B]
  type Or[A <: TBool, B <: TBool] = A#Or[B]
  type Not[A <: TBool] = A#Not
}
