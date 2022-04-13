package pl.otros.logview.scala

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class TreeParserTest extends WordSpecLike with Matchers {

  "Map" should {
    "with one value should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a -> b")
      val expected: MapEntry = MapEntry("a", "b")
      println(s"Expected $expected")
      println(s"Result: ${toMapEntries.head}")
      toMapEntries shouldBe List(expected)
    }

    "with two values should split" in {
      println("with two values should split")
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a -> b, x -> y")
      toMapEntries shouldBe List(MapEntry("a", "b"), MapEntry("x", "y"))
    }

    "with three values should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a -> b, x -> y, z -> d")
      toMapEntries shouldBe List(MapEntry("a", "b"), MapEntry("x", "y"), MapEntry("z", "d"))
    }

    "with one advance key should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a(1,2,3) -> b")
      toMapEntries shouldBe List(MapEntry("a(1,2,3)", "b"))
    }

    "with two advance keys should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a(1,2,3) -> b1, a(2,2,3) -> b2")
      toMapEntries shouldBe List(MapEntry("a(1,2,3)", "b1"), MapEntry("a(2,2,3)", "b2"))
    }


    "with one advance value should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a -> b(1,2,3)")
      toMapEntries shouldBe List(MapEntry("a", "b(1,2,3)"))
    }


    "with two advance values should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a -> b(1,2,3),  b -> b(2,2,3)")
      toMapEntries shouldBe List(MapEntry("a", "b(1,2,3)"), MapEntry("b", "b(2,2,3)"))
    }


    "with advance key and value should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a(1,3) -> b(1,2,3)")
      toMapEntries shouldBe List(MapEntry("a(1,3)", "b(1,2,3)"))
    }

    "with two advance key and value should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a(1,3) -> b(1,2,3), a(1,2) -> b(2,2,3)")
      toMapEntries shouldBe List(MapEntry("a(1,3)", "b(1,2,3)"), MapEntry("a(1,2)", "b(2,2,3)"))
    }

    "with Map as a key should split" in {
      val s = "Map(a -> x) -> b" //Map(Map(a -> x)) -> b)
      println(s"Testing $s")
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries(s)
      toMapEntries shouldBe List(MapEntry("Map(a -> x)", "b"))
    }


    "with Map as a value should split" in {
      val toMapEntries: List[MapEntry] = TreeParser.stringToMapEntries("a -> Map(b -> c)")
      toMapEntries shouldBe List(MapEntry("a", "Map(b -> c)"))
    }

  }

  "TreeParse" should {

    "parse simple case class" in {
      val s = "CaseClass(A)"
      val tree = TreeParser.parseTree(s)
      tree shouldBe Branch("CaseClass", List(SimpleValue("A")))
    }

    "parse Map" in {
      val s = "Map(A -> B, C -> D)"
      val tree = TreeParser.parseTree(s)
      tree shouldBe MapValue(
        Map(SimpleValue("A") -> SimpleValue("B"),
          SimpleValue("C") -> SimpleValue("D"))
      )
    }

    "parse List" in {
      val s = "List(A, B, C, D)"
      val tree = TreeParser.parseTree(s)
      tree shouldBe ListValue(List(
        SimpleValue("A"),
        SimpleValue("B"),
        SimpleValue("C"),
        SimpleValue("D")
      ))
    }

    "parse Set" in {
      val s = "Set(A, B, C, D)"
      val tree = TreeParser.parseTree(s)
      tree shouldBe SetValue(List(
        SimpleValue("A"),
        SimpleValue("B"),
        SimpleValue("C"),
        SimpleValue("D")
      ))
    }

  }

}
