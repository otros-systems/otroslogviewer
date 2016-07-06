package pl.otros.logview.scala

import scala.language.implicitConversions

object TreeParser {

  implicit def parseTree(s: String): Tree = {
    val m = "\\w+\\(".r().pattern.matcher(s)
    val find: Boolean = m.find()
    if (find) {
      val first = s.indexOf('(')
      val last = s.lastIndexOf(')')
      val value: String = s.substring(first + 1, last)
      val split: List[String] = magicSplit(value)
      val name: String = s.substring(0, s.indexOf('('))
      name match {
        case "Map" => MapValue(stringToMapEntries(value).map(me => parseTree(me.key) -> parseTree(me.value)).toMap)
        case "List" => new ListValue(magicSplit(value).map(parseTree))
        case "Set" => new SetValue(magicSplit(value).map(parseTree))
        case _ => new Branch(name, split.map(parseTree))
      }
    } else {
      new SimpleValue(s)
    }

  }

  private def magicSplit(s: String): List[String] = {
    //Skip "' for now
    val charList = s.toCharArray.toList
    case class SplitAcc(count: Int, current: Int, positions: List[Int]) {
      def move() = this.copy(current = current + 1)
    }
    val splitPositions: List[Int] = charList.foldLeft(SplitAcc(0, 0, List.empty[Int])) {
      (acc, c) =>
        val x: SplitAcc = c match {
          case '(' => acc.copy(count = acc.count + 1).move()
          case ')' => acc.copy(count = acc.count - 1).move()
          case ',' if acc.count == 0 => acc.copy(positions = acc.current :: acc.positions).move()
          case _ => acc.move()
        }
        x
    }.positions.reverse
    val at: List[String] = splitAt(s, splitPositions)
    at match {
      case List("") => Nil
      case _ => at
    }
  }

  def stringToMapEntries(s: String): List[MapEntry] = {
    val pairs: List[String] = magicSplit(s)
    pairs.map { s =>
      val keyValue: (String, String) = splitMap(s)
      MapEntry(keyValue._1, keyValue._2)
    }
  }

  private def splitMap(s: String): (String, String) = {
    case class SplitAcc(count: Int = 0,
                        current: Int = 0,
                        positions: List[Int] = List.empty[Int],
                        prefix: List[Char] = List.empty) {
      def move(char: Char) = {
        val newPrefix = this.prefix match {
          case Nil => char :: Nil
          case List(c1, c2, c3) => char :: c1 :: c2 :: Nil
          case l: List[Char] => char :: l
        }
        this.copy(current = current + 1, prefix = newPrefix)
      }
    }
    val splitPositions: List[Int] = s.foldLeft(SplitAcc()) {
      (acc, c) =>
        c match {
          case '(' => acc.copy(count = acc.count + 1).move(c)
          case ')' => acc.copy(count = acc.count - 1).move(c)
          case ' ' if acc.count == 0 && acc.prefix == List('>', '-', ' ') =>
            acc.copy(positions = acc.current :: acc.positions).move(c)
          case _ => acc.move(c)
        }
    }.positions.reverse
    val at: List[String] = splitAt(s, splitPositions)
    val key = at.head.substring(0, at.head.length - 3)
    val value = at.tail.headOption.getOrElse("")
    (key, value)
  }

  def splitAt(s: String, positions: List[Int]): List[String] = {
    val splitStarts = -1 :: positions
    val splitEnds = (s.length :: positions.reverse).reverse
    val splitPositionsList = splitStarts.zip(splitEnds)
    splitPositionsList.map(p => s.substring(p._1 + 1, p._2).trim)
  }

}
