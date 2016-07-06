package pl.otros.logview.scala

import TreeParser.parseTree

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
          val notCaseClass = s.substring(acc.lastEnd + 1, fragment.startPosition - 1)
          val caseClass = s.substring(fragment.startPosition, fragment.lastPosition + 1)
          val caseClassFormatted = new SimpleTreePrinter().printTree(caseClass)
          Accumulator(caseClassFormatted :: notCaseClass :: acc.strings, fragment.lastPosition)
      }
      val start: Int = fragments.lastOption.map(_.lastPosition + 1).getOrElse(0)
      val resultList = (s.substring(start, s.length) :: r.strings).reverse
      resultList.mkString("\n")
    }

    parseAndPrint(string)
  }

  def main(args: Array[String]) {
    println(Formatter.format("1. ala ma kota"))
    println(Formatter.format("2. ala Ma(false) kota"))
    println(Formatter.format("3. ala Map(a -> b) kota"))
    println(Formatter.format("4. ala Some(B) second AB(C, D, G(H), Map(a -> b)) kota Map(a -> b) !"))
    println(Formatter.format("Message with case class: AB(C, D, G(H), Map(a -> b, c -> gg), List(element 1, element 2), Set(element 1, element 2, Some(true), Cad(prop1, prop2)))"))
  }
}
