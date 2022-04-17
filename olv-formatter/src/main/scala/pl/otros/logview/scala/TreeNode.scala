package pl.otros.logview.scala

sealed trait Tree

case class Branch(name: String, values: List[Tree]) extends Tree

case class SimpleValue(value: Any) extends Tree

case class MapValue(map: Map[Tree, Tree]) extends Tree

case class ListValue(values: List[Tree]) extends Tree

case class SetValue(values: List[Tree]) extends Tree

case class MapEntry(key: String, value: String)

trait TreePrinter {
  def printTree(tree: Tree): String
}




