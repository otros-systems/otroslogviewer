package pl.otros.logview.scala

case class Fragment(startPosition: Int, lastPosition: Int) {
  def stringPart(s: String): String = s.substring(startPosition, lastPosition)
}

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
            case a: Acc if !c.isWhitespace && a.prevChar.isWhitespace && a.balance == 0 && !a.inValue =>
              a.copy(lastStart = a.position, inValue = true)
            case a: Acc if c == '(' => a.copy(balance = a.balance + 1)
            case a: Acc if c == ')' && a.balance == 1 && a.inValue =>
              a.copy(balance = a.balance - 1,
                fragments = Fragment(a.lastStart, a.position + 1) :: a.fragments,
                inValue = false
              )
            case a: Acc if c == ')' => a.copy(balance = a.balance - 1)
            case a: Acc if a.inValue && c.isWhitespace && a.balance == 0 =>
              a.copy(inValue = false)
            case a: Acc if a.inValue && c == '.' && a.balance == 0 =>
              a.copy(inValue = false)
            case a: Acc => a
          }
        r.move().copy(prevChar = c)
    }
    ac.fragments.reverse
  }
}
