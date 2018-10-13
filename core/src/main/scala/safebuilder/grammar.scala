package safebuilder

object grammar {

  case class FieldLexeme(lemma: String) {
    lazy val capitalized: String = lemma.capitalize
    lazy val withForm: String = s"with$capitalized"
    lazy val appendForm: String = s"append$capitalized"
    lazy val prependForm: String = s"prepend$capitalized"
    lazy val enabledForm: String = s"${lemma}Enabled"
    lazy val disabledForm: String = s"${lemma}Disabled"
    lazy val added: String = s"${capitalized}Added"
    lazy val singular: String =
      if (lemma.endsWith("s")) lemma.dropRight(1)
      else s"_$lemma" // adding underscore so that it's distinguished from the plural version
  }

  case class ClassLexeme(lemma: String) {
    lazy val capitalized: String = lemma.capitalize
    lazy val builder: String = s"${capitalized}Builder"
    lazy val guard: String = s"${capitalized}Guard"
  }

}
