package pl.otros.logview.scala

import pl.otros.logview.scala.TreeParser.parseTree

object Formatter {

  def isFormattingNeeded(string: String): Boolean = {
    Detector.find(string).nonEmpty
  }

  def format(string: String): String = {
    println(s"Processing $string\nLength: ${string.size}")
    def parseAndPrint(s: String): String = {
      val fragments: List[Fragment] = Detector.find(s)
      case class Accumulator(strings: List[String], lastEnd: Int)
      val initialAcc: Accumulator = new Accumulator(List.empty[String], -1)
      val r = fragments.foldLeft(initialAcc) {
        (acc, fragment) =>
          println(s"\n$acc -> $fragment")
          val notCaseClass = s.substring(acc.lastEnd + 1, fragment.startPosition).trim
          println(s"Not case class: $notCaseClass")
          val caseClass = fragment.stringPart(s)
          println(s"Case class: $caseClass")
          val caseClassFormatted = new SimpleTreePrinter().printTree(caseClass)
          println(s"Case class formatted: $caseClassFormatted")
          Accumulator(caseClassFormatted :: notCaseClass :: acc.strings, fragment.lastPosition)
      }
      val start: Int = fragments.lastOption.map(_.lastPosition).getOrElse(0)
      val ending: String = s.substring(start, s.length)
      println(s"Adding ending $ending")
      val resultList = (ending :: r.strings).reverse
      resultList.mkString("\n").trim()
    }

    val r = parseAndPrint(string).trim
    println(s"Result is '$r'")
    r
  }

  def main(args: Array[String]) {
    println(Formatter.format("1. ala ma kota"))
    println(Formatter.format("2. ala Ma(false) kota"))
    println(Formatter.format("3. ala Map(a -> b) kota"))
    println(Formatter.format("4. ala Some(B) second AB(C, D, G(H), Map(a -> b)) kota Map(a -> b) !"))
    println(Formatter.format("Extracted already processed: List() on UserId(Scca3b)"))
  }
}
