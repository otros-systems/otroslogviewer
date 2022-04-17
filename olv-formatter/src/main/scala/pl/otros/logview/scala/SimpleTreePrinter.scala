package pl.otros.logview.scala

class SimpleTreePrinter extends TreePrinter {

  sealed trait ChildOf

  case object ChildOfMapValue extends ChildOf

  case object ChildOfList extends ChildOf

  case object ChildOfSet extends ChildOf

  case object ChildOfOther extends ChildOf

  case object ChildOfStart extends ChildOf

  case object ChildOfMapKey extends ChildOf

  def printTree(tree: Tree): String = {
    def print(tab: String, t: Tree, isTail: Boolean, childOf: ChildOf): String = {
      val pr1 = childOf match {
        case ChildOfStart => "*"
        case ChildOfList => "○"
        case ChildOfMapValue => "→"
        case ChildOfMapKey => "▷"
        case ChildOfSet => "□"
        case ChildOfOther => "─"
      }

      val pr = if (childOf == ChildOfStart) {
        " " + pr1
      } else if (isTail) {
        "└" + pr1
      } else {
        "├" + pr1
      }
      val append = if (isTail || tab.isEmpty) "  " else "│ "
      t match {

        case Branch(name, Nil) =>
          val p = s"$pr── "
          s"$tab$p$name: <EMPTY>"

        case Branch(name, sv :: Nil) if sv.isInstanceOf[SimpleValue] =>
          val p = s"$pr── "
          s"$tab$p$name: ${sv.asInstanceOf[SimpleValue].value}"

        case Branch(name, value) =>
          val vStrings: List[String] = value.
            zipWithIndex
            .map(x => print(tab + append, x._1, x._2 == value.size - 1, ChildOfOther))
          val valueSting = vStrings.mkString("\n")
          val p = s"$pr┬─ "
          s"$tab$p$name:\n$valueSting"

        case SimpleValue(value) =>
          val p = s"$pr── "
          s"$tab$p$value"

        case MapValue(m) if m.isEmpty =>
          val p = s"$pr── "
          s"$tab${p}Map: <EMPTY>"

        case MapValue(map) =>
          val p = s"$pr┬─ "
          val r = map
            .zipWithIndex
            .map { kv =>
              val lastElement: Boolean = kv._2 == map.size - 1
              val appendValue = if (lastElement) "     " else "│    "
              val tab1: String = tab + append
              s"${print(tab1, kv._1._1, lastElement, ChildOfMapKey)}\n${print(tab1 + appendValue, kv._1._2, isTail = true, ChildOfMapValue)}"
            }
            .mkString(s"${p}Map (${map.size}):\n", "\n", "")
          s"$tab$r"

        case ListValue(Nil) =>
          val p = s"$pr── "
          s"$tab${p}List: <EMPTY>"

        case ListValue(list) =>
          val p = s"$pr┬─ "
          list
            .zipWithIndex
            .map(v => s"${print(tab + append, v._1, v._2 == list.size - 1, ChildOfList)}")
            .mkString(s"$tab${p}List (${list.size}):\n", "\n", "")

        case SetValue(Nil) =>
          val p = s"$pr── "
          s"$tab${p}Set: <EMPTY>"

        case SetValue(set) =>
          val p = s"$pr┬─ "
          set
            .zipWithIndex
            .map(v => s"${print(tab + append, v._1, v._2 == set.length - 1, ChildOfSet)}")
            .mkString(s"$tab${p}Set (${set.size}):\n", "\n", "")
      }

    }
    print("", tree, isTail = false, ChildOfStart)
  }

}
