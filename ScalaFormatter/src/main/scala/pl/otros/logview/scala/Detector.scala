package pl.otros.logview.scala

case class Fragment(startPosition: Int, lastPosition: Int)

case class Acc(position: Int = 0,
               lastStart: Int = -1,
               inValue: Boolean = false,
               balance: Int = 0,
               prevChar: Char = ' ',
               fragments: List[Fragment] = List.empty[Fragment]) {
  def move() = this.copy(position = position + 1)
}

object Detector {
  def find(s: String) = {
    val ac = s.foldLeft(Acc()) {
      (acc, c) =>
        val r: Acc =
          acc match {
            case a: Acc if !c.isWhitespace && a.prevChar.isWhitespace && a.balance == 0 =>
              a.copy(lastStart = a.position, inValue = true)
            case a: Acc if c == '(' => a.copy(balance = a.balance + 1)
            case a: Acc if c == ')' && a.balance == 1 =>
              a.copy(balance = a.balance - 1,
                fragments = Fragment(a.lastStart, a.position) :: a.fragments,
                inValue = false
              )
            case a: Acc if c == ')' => a.copy(balance = a.balance - 1)
            case a: Acc if a.inValue && c.isWhitespace => a.copy(inValue = false)
            case a: Acc => a
          }
        r.move().copy(prevChar = c)
    }
    ac.fragments.reverse
  }

  def main(args: Array[String]) {
    val event1 = """Received  Object(Set(A, B, D, true, false), List(), Set(), List(A, B(x,v), C(Some(true)), D), Map(), Map(A(true) -> B, C -> X, v(B(C),Some(false)) -> g))"""

    def parseAndPrint(s:String): Unit = {
      val fragments = find(s)
      println(s"Result: $fragments")
      println(s"${fragments.map(f => s.substring(f.startPosition, f.lastPosition + 1)).mkString("Fragments:\n", "\n", "\n")}")

      fragments
        .map(f => s.substring(f.startPosition, f.lastPosition + 1))
        .map(f => TreeParser.parseTree(f))
        .map(f => new AdvancedTreePrinter().printTree(f))
        .foreach(f => println(f))
    }
    parseAndPrint(event1)

  }

}
