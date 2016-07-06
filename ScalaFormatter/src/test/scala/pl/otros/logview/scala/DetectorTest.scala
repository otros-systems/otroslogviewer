package pl.otros.logview.scala

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class DetectorTest extends WordSpecLike with Matchers {

  "Detector" should {
    "detect simple case class" in {
      val s: String = "MyClass(a)"
      val find: List[Fragment] = Detector.find(s)
      println(s"'${s.substring(0, s.size)}'")
      find shouldBe List(Fragment(0, s.size))
      find.map(f => s.substring(f.startPosition,f.lastPosition)) shouldBe List(s)
    }
    "detect simple case class in message" in {
      val s: String = "message m MyClass(b) a b c"
      val find: List[Fragment] = Detector.find(s)
      find.map(f => s.substring(f.startPosition,f.lastPosition)) shouldBe List("MyClass(b)")
      find shouldBe List(Fragment(10,20))
    }

    "detect complex case class in message" in {
      val s: String = "message m MyClass(b, Some(C), G(A,B)) a b c"
      val find: List[Fragment] = Detector.find(s)
      find.map(f => s.substring(f.startPosition,f.lastPosition)) shouldBe List("MyClass(b, Some(C), G(A,B))")
    }

    "detect simple Map in message" in {
      val map = "Map(b -> c)"
      val s: String = s"message m $map a a"
      val find: List[Fragment] = Detector.find(s)
      find.map(f => s.substring(f.startPosition,f.lastPosition)) shouldBe List(map)
    }

    "detect 2 elements in message" in {
      val element1 = "Map(b -> c)"
      val element2 = "Some(Bum(C, D, F))"
      val s: String = s"message m $element1 a $element2 a"
      val find: List[Fragment] = Detector.find(s)
      find.map(f => s.substring(f.startPosition,f.lastPosition)) shouldBe List(element1, element2)
    }

    "don't detect elements without '(' balance" in {
      val s: String = s"message m Map(A -> B(C) a"
      val find: List[Fragment] = Detector.find(s)
      find shouldBe Nil
    }

    "ignore stacktrace element" in {
      val s: String = "\tat java.io.FileInputStream.<init>(FileInputStream.java)"
      val find: List[Fragment] = Detector.find(s)
      find.map(f=>s.substring(f.startPosition,f.lastPosition)) shouldBe Nil
      find shouldBe Nil

    }
  }
}
