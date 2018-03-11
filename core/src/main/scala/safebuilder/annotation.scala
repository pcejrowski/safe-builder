package safebuilder


import safebuilder.grammar.{ClassLexeme, FieldLexeme}

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.collection.immutable.Seq
import scala.language.experimental.macros
import scala.reflect.api.Trees
import scala.reflect.macros.whitebox

object annotation {

  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class builder extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro Builder.impl
  }

  object Builder {
    def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._

      def extractCaseClassParts(classDecl: ClassDef) = classDecl match {
        case q"case class $className(..$fields) extends ..$parents { ..$body }" =>
          (className.asInstanceOf[TypeName], fields)
        case _ => c.abort(c.enclosingPosition, "Invalid annottee: not a case class declaration")
      }

      case class CompanionObject(compDeclOpt: Option[ModuleDef], lex: ClassLexeme, fields: Seq[Field]) {
        def expr(): Expr[Any] = {
          val falseGuards = fields.map(f => q"type ${TypeName(f.lex.added)} = TFalse")
          val apply =
            q"""
               def apply() = new ${TypeName(lex.builder)}[${TypeName(lex.guard)}{ ..$falseGuards }]
             """
          compDeclOpt.map { compDecl =>
            // Add the formatter to the existing companion object
            val q"object $obj extends ..$bases { ..$body }" = compDecl
            c.Expr(
              q"""
                 object $obj extends ..$bases {
                   $body
                   $apply
                 }
               """
            )
          }.getOrElse {
            c.Expr(q""" object ${TermName(lex.lemma)} { $apply }""")
          }
        }
      }

      case class Guard(lex: ClassLexeme, fields: Seq[Field]) {
        def expr(): Expr[Any] = {
          val types = fields
            .map(f => q"type ${TypeName(f.lex.added)} <: TBool")
          c.Expr(
            q"""
               trait ${TypeName(lex.guard)}{
                 ..$types
               }
             """
          )
        }
      }
      case class Builder(lex: ClassLexeme, fields: Seq[Field]) {
        def expr(): Expr[Any] = {
          val nones = fields.map(_ => q"None")

          val args = fields.map(f => ValDef(Modifiers(), f.name, AppliedTypeTree(Ident(TypeName("Option")), List(f.tpe)), q"None"))

          val gets = fields.map(f => q"${f.name}.get")

          val conditions = fields
            .filter(_.isRequired)
            .map(f => SelectFromTypeTree(Ident(TypeName("G")), TypeName(f.lex.added)): Tree)
          val buildCondition = conditions.size match {
            case 0 => Ident(TypeName("TTrue")) : Tree
            case 1 => conditions.head
            case _ => conditions.reduce((a, b) => AppliedTypeTree(Ident(TypeName("And")), List(a, b)): Tree)
          }

          c.Expr(
            q"""
              class ${TypeName(lex.builder)}[G <: ${TypeName(lex.guard)}] (..$args) {
                import safebuilder.phantoms._
                ..${fields.flatMap(_.mutators(fields))}
                def build(implicit ev: $buildCondition =:= TTrue): ${TypeName(lex.lemma)}= {
                  ${TermName(lex.lemma)}(..$gets)
                }
              }
            """
          )
        }
      }

      case class Field(parent: ClassLexeme, name: TermName, tpe: Tree, default: Tree) {
        lazy val lex = FieldLexeme(name.toString)

        def isRequired: Boolean = !isOption

        def isOption: Boolean = isApplied(Some("Option"))

        def isSeq: Boolean = isApplied(Some("Seq"))

        def isEither: Boolean = isApplied(Some("Either"))

        def isApplied(implicit typeName: Option[String] = None): Boolean =
          (tpe, typeName) match {
            case (AppliedTypeTree(Ident(TypeName(tpeName)), _), Some(typeName)) => tpeName == typeName
            case (AppliedTypeTree(Ident(TypeName(_)), _), None) => true
            case _ => false
          }

        def mutators(fields: Seq[Field]): Seq[Tree] = {
          def args(implicit param: Option[Any] = None) = fields.map {
            case f if f == this && f.isOption => q"Some(Some(${f.name}))"
            case f if f == this && f.isEither && param.isDefined => q"Some(${TermName(param.get.toString)}(${f.name}))"
            case f if f == this && param.isDefined => q"Some(${Literal(Constant(param.get))})"
            case f if f == this => q"Some(${f.name})"
            case f => q"${f.name}"
          }

          tpe match {
            case AppliedTypeTree(Ident(TypeName("Option")), List(innerTpe)) =>
              Seq(
                q"""
                    def ${TermName(lex.withForm)}(${TermName(lex.lemma)}: $innerTpe)(implicit ev: G#${TypeName(lex.added)} =:= TFalse) =
                      new ${TypeName(parent.builder)}[G { type ${TypeName(lex.added)} = TTrue }](..$args)
                 """
              )
            case AppliedTypeTree(Ident(TypeName("Either")), List(leftTpe, rightTpe)) =>
              Seq(
                q"""
                    def ${TermName(lex.withForm)}(${TermName(lex.lemma)}: $leftTpe)(implicit ev: G#${TypeName(lex.added)} =:= TFalse) =
                      new ${TypeName(parent.builder)}[G { type ${TypeName(lex.added)} = TTrue }](..${args(Some("Left"))})
                 """,
                q"""
                    def ${TermName(lex.withForm)}(${TermName(lex.lemma)}: $rightTpe)(implicit ev: G#${TypeName(lex.added)} =:= TFalse) =
                      new ${TypeName(parent.builder)}[G { type ${TypeName(lex.added)} = TTrue }](..${args(Some("Right"))})
                 """
              )
            case Ident(TypeName("Boolean")) =>
              Seq(
                q"""
                    def ${TermName(lex.enabledForm)}(implicit ev: G#${TypeName(lex.added)} =:= TFalse) =
                      new ${TypeName(parent.builder)}[G { type ${TypeName(lex.added)} = TTrue }](..${args(Some(true))})
                 """,
                q"""
                    def ${TermName(lex.disabledForm)}(implicit ev: G#${TypeName(lex.added)} =:= TFalse) =
                      new ${TypeName(parent.builder)}[G { type ${TypeName(lex.added)} = TTrue }](..${args(Some(false))})
                 """
              )
            case t =>
              Seq(
                q"""
                    def ${TermName(lex.withForm)}(${TermName(lex.lemma)}: $t)(implicit ev: G#${TypeName(lex.added)} =:= TFalse) =
                      new ${TypeName(parent.builder)}[G { type ${TypeName(lex.added)} = TTrue }](..$args)
                 """
              )
          }
        }
      }

      def parse(parent: ClassLexeme, fields: scala.Seq[Trees#Tree]): Seq[Field] = fields
        .asInstanceOf[List[ValDef]]
        .map {
          case ValDef(_, termName, tpe, rhs) => Field(parent, termName, tpe, rhs)
        }

      def pimpCaseClass(classDef: c.universe.ClassDef, compDecl: Option[ModuleDef] = None): Seq[Expr[Any]] = {
        val (className, fields) = extractCaseClassParts(classDef)
        val lex = ClassLexeme(className.toString)
        val fs = parse(lex, fields)

        Seq(
          c.Expr(q"import safebuilder.phantoms._"),
          Guard(lex, fs).expr(),
          c.Expr(q"$classDef"),
          CompanionObject(compDecl, lex, fs).expr(),
          Builder(lex, fs).expr()
        )
      }

      val pimpedClasses = annottees.map(_.tree) match {
        case (classDecl: ClassDef) :: Nil => pimpCaseClass(classDecl)
        case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => pimpCaseClass(classDecl, Some(compDecl))
        case _ => c.abort(c.enclosingPosition, "Invalid annottee: not a class declaration")
      }
      c.Expr(q"..$pimpedClasses")
    }
  }

}

