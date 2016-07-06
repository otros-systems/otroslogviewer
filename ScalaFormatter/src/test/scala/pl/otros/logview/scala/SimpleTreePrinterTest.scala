package pl.otros.logview.scala

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class SimpleTreePrinterTest extends WordSpecLike with Matchers {

  "SimpleTreePrinter" should {
    "print empty list" in {
      val tree: Tree = ListValue(List.empty[Tree])
      val expected =""" *── List: <EMPTY>"""
      val printed: String = new SimpleTreePrinter().printTree(tree)


      printed shouldBe expected
    }

    "print 1 element list" in {
      val tree: Tree = ListValue(List(SimpleValue("A")))
      val expected =
        """ *┬─ List (1):
          |  └○── A""".stripMargin
      val printed: String = new SimpleTreePrinter().printTree(tree)
      printed shouldBe expected
    }
    "print 3 element list" in {
      val tree: Tree = ListValue(List(SimpleValue("A"), SimpleValue("B"), SimpleValue("C")))
      val expected =
        """ *┬─ List (3):
          |  ├○── A
          |  ├○── B
          |  └○── C"""
          .stripMargin
      val printed: String = new SimpleTreePrinter().printTree(tree)
      printed shouldBe expected
    }

    "print empty set" in {
      val tree: Tree = SetValue(List.empty[Tree])
      val expected =""" *── Set: <EMPTY>"""
      val printed: String = new SimpleTreePrinter().printTree(tree)
      printed shouldBe expected
    }

    "print 1 element Set" in {
      val tree: Tree = SetValue(List(SimpleValue("A")))
      val expected =
        """ *┬─ Set (1):
          |  └□── A""".stripMargin
      val printed: String = new SimpleTreePrinter().printTree(tree)


      printed shouldBe expected
    }
    "print 3 element Set" in {
      val tree: Tree = SetValue(List(SimpleValue("A"), SimpleValue("B"), SimpleValue("C")))
      val expected =
        """ *┬─ Set (3):
          |  ├□── A
          |  ├□── B
          |  └□── C"""
          .stripMargin
      val printed: String = new SimpleTreePrinter().printTree(tree)


      printed shouldBe expected
    }
    "print empty Map" in {
      val tree: Tree = MapValue(Map.empty)
      val expected =""" *── Map: <EMPTY>"""
      val printed: String = new SimpleTreePrinter().printTree(tree)


      printed shouldBe expected
    }

    "print 1 element Map" in {
      val tree: Tree = MapValue(Map(SimpleValue("A") -> SimpleValue("1")))
      val expected =
        """ *┬─ Map (1):
          |  └▷── A
          |       └→── 1""".stripMargin
      val printed: String = new SimpleTreePrinter().printTree(tree)


      printed shouldBe expected
    }
    "print 3 element Map" in {
      val tree: Tree = MapValue(Map(
        SimpleValue("A") -> SimpleValue("1"),
        SimpleValue("B") -> SimpleValue("2"),
        SimpleValue("C") -> SimpleValue("3")
      ))
      val expected =
        """ *┬─ Map (3):
          |  ├▷── A
          |  │    └→── 1
          |  ├▷── B
          |  │    └→── 2
          |  └▷── C
          |       └→── 3""".stripMargin
      val printed: String = new SimpleTreePrinter().printTree(tree)
      printed shouldBe expected
    }
  }
}
