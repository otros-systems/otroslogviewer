package pl.otros.logview.scala

import pl.otros.logview.scala.TreeParser.parseTree

object Formatter {

  def isFormattingNeeded(string: String): Boolean = {
    Detector.find(string).nonEmpty
  }

  def format(string: String): String = {
    def parseAndPrint(s: String): String = {
      val fragments: List[Fragment] = Detector.find(s)
      case class Accumulator(strings: List[String], lastEnd: Int)
      val initialAcc: Accumulator = new Accumulator(List.empty[String], -1)
      val r = fragments.foldLeft(initialAcc) {
        (acc, fragment) =>
          val notCaseClass = s.substring(acc.lastEnd + 1, fragment.startPosition).trim
          val caseClass = fragment.stringPart(s)
          val caseClassFormatted = new SimpleTreePrinter().printTree(caseClass)
          Accumulator(caseClassFormatted :: notCaseClass.trim :: acc.strings, fragment.lastPosition)
      }
      val start: Int = fragments.lastOption.map(_.lastPosition).getOrElse(0)
      val ending: String = s.substring(start, s.length).trim
      val resultList: List[String] = (ending :: r.strings).reverse
      resultList
        .filter(_.length >0)
        .mkString("\n")
    }
    parseAndPrint(string)
  }
}
