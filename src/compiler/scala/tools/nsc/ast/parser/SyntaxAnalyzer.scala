/* NSC -- new Scala compiler
 * Copyright 2005-2013 LAMP/EPFL
 * @author Martin Odersky
 */

package scala.tools.nsc
package ast.parser

import javac._

/** An nsc sub-component.
 */
abstract class SyntaxAnalyzer extends SubComponent with Parsers with MarkupParsers with Scanners with JavaParsers with JavaScanners {

  val phaseName = "parser"

  def newPhase(prev: Phase): StdPhase = new ParserPhase(prev)

  class ParserPhase(prev: scala.tools.nsc.Phase) extends StdPhase(prev) {
    override val checkable = false
    override val keepsTypeParams = false

    def apply(unit: global.CompilationUnit) {
      import global._
      informProgress("parsing " + unit)
      // if the body is already filled in, do nothing
      // otherwise compileLate is going to overwrite bodies of synthetic source files
      if (unit.body == EmptyTree) {
        unit.body =
          if (unit.isJava) new JavaUnitParser(unit).parse()
          else if (reporter.incompleteHandled) newUnitParser(unit).parse()
          else newUnitParser(unit).smartParse()
      }

      if (settings.Yrangepos.value && !reporter.hasErrors)
        validatePositions(unit.body)
    }
  }
}

