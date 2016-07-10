package pl.otros.logview.scala

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class FormatterTest extends WordSpecLike with Matchers {

  "Formatter" should {
    "format string without case class" in {
      val s: String = "Some message goes here"
      val expected = "Some message goes here"
      val f = Formatter.format(s)
      f shouldBe expected
    }

    "format string  - case class only" in {
      val s: String = "Some(Value)"
      val expected = " *── Some: Value"
      val f = Formatter.format(s)
      f shouldBe expected
    }

    "format string starting with case class" in {
      val s: String = "Some(Value) string"
      val expected =
        """ *── Some: Value
          |string""".stripMargin
      val f = Formatter.format(s)
      f shouldBe expected
    }
    "format string ending with case class" in {
      val s: String = "String Some(Value)"
      val expected =
        """String
          | *── Some: Value""".stripMargin
      val f = Formatter.format(s)
      f shouldBe expected

    }
    "format string case class in the middle" in {
      val s: String = "Start Some(Value) end"
      val expected =
        """Start
          | *── Some: Value
          |end""".stripMargin
      val f = Formatter.format(s)
      f shouldBe expected
    }
    "format string with 2 case classes in the middle" in {
      val s: String = "Start Some(Value) middle Some(Value2) end"
      val expected =
        """Start
          | *── Some: Value
          |middle
          | *── Some: Value2
          |end""".stripMargin
      val f = Formatter.format(s)
      f shouldBe expected
    }
    "fromat complex example" in {
      val s = "ala Some(B) second AB(C, D, G(H), Map(a -> b)) kota Map(a -> b) !"
      val expected =
        """ala
          | *── Some: B
          |second
          | *┬─ AB:
          |  ├─── C
          |  ├─── D
          |  ├─── G: H
          |  └─┬─ Map (1):
          |    └▷── a
          |         └→── b
          |kota
          | *┬─ Map (1):
          |  └▷── a
          |       └→── b
          |!""".stripMargin

      Formatter.format(s) shouldBe expected
    }
  }
}
