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
      test(tree, expected)
    }

    "print 1 element list" in {
      val tree: Tree = ListValue(List(SimpleValue("A")))
      val expected =
        """ *┬─ List (1):
          |  └○── A""".stripMargin
      test(tree, expected)
    }

    "print 3 element list" in {
      val tree: Tree = ListValue(List(SimpleValue("A"), SimpleValue("B"), SimpleValue("C")))
      val expected =
        """ *┬─ List (3):
          |  ├○── A
          |  ├○── B
          |  └○── C"""
          .stripMargin
      test(tree, expected)
    }

    "print empty set" in {
      val tree: Tree = SetValue(List.empty[Tree])
      val expected =""" *── Set: <EMPTY>"""
      test(tree, expected)
    }

    "print 1 element Set" in {
      val tree: Tree = SetValue(List(SimpleValue("A")))
      val expected =
        """ *┬─ Set (1):
          |  └□── A""".stripMargin
      test(tree, expected)
    }

    "print 3 element Set" in {
      val tree: Tree = SetValue(List(SimpleValue("A"), SimpleValue("B"), SimpleValue("C")))
      val expected =
        """ *┬─ Set (3):
          |  ├□── A
          |  ├□── B
          |  └□── C"""
          .stripMargin
      test(tree, expected)
    }

    "print empty Map" in {
      val tree: Tree = MapValue(Map.empty)
      val expected =""" *── Map: <EMPTY>"""
      test(tree, expected)
    }

    "print 1 element Map" in {
      val tree: Tree = MapValue(Map(SimpleValue("A") -> SimpleValue("1")))
      val expected =
        """ *┬─ Map (1):
          |  └▷── A
          |       └→── 1""".stripMargin
      test(tree, expected)
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
      test(tree, expected)
    }

    "print nested object" in {
      val tree: Tree = Branch("Name",
        List(
          SimpleValue("A"),
          SimpleValue("B"),
          Branch("Name2",
            List(
              SimpleValue("C"),
              SimpleValue("D")
            ))))
      val expected =
        """
          | *┬─ Name:
          |  ├─── A
          |  ├─── B
          |  └─┬─ Name2:
          |    ├─── C
          |    └─── D
        """.stripMargin
      test(tree, expected)
    }

    "print nested object with list, set and map" in {
      val tree: Tree = Branch("Name",
        List(
          SimpleValue("A"),
          SimpleValue("B"),
          Branch("Name2",
            List(
              SimpleValue("C"),
              SimpleValue("D")
            )
          ),
          MapValue(Map(
            SimpleValue("mk1") -> SimpleValue("mv1"),
            SimpleValue("mk2") -> SimpleValue("mv2"),
            SimpleValue("mk3") -> SimpleValue("mv3")
          ))
          , SetValue(List(
            SimpleValue("s1"),
            SimpleValue("s2"),
            SimpleValue("s3")
          ))
        )
      )
      val expected =
        """
          | *┬─ Name:
          |  ├─── A
          |  ├─── B
          |  ├─┬─ Name2:
          |  │ ├─── C
          |  │ └─── D
          |  ├─┬─ Map (3):
          |  │ ├▷── mk1
          |  │ │    └→── mv1
          |  │ ├▷── mk2
          |  │ │    └→── mv2
          |  │ └▷── mk3
          |  │      └→── mv3
          |  └─┬─ Set (3):
          |    ├□── s1
          |    ├□── s2
          |    └□── s3
        """.stripMargin
      test(tree, expected)
    }
  }

  "print empty branch" in {
    val tree = Branch("Parent", Nil)
    val expected =
      """
        | *── Parent: <EMPTY>
      """.stripMargin

    test(tree, expected)
  }

  "print branch wtih single value" in {
    val tree = Branch("Parent", List(SimpleValue("Value")))
    val expected =
      """
        | *── Parent: Value
      """.stripMargin

    test(tree, expected)
  }

  def test(tree: Tree, expected: String) = {
    val printed: String = new SimpleTreePrinter().printTree(tree)
    val expectedWithoutEmptyLines = expected.linesIterator.filter(!_.trim.isEmpty).mkString("\n")
    printed shouldBe expectedWithoutEmptyLines
  }

}
