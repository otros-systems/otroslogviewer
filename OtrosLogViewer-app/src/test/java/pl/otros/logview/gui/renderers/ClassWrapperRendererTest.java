package pl.otros.logview.gui.renderers;

import com.google.common.base.Joiner;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class ClassWrapperRendererTest {

  private ClassWrapperRenderer underTest;

  @BeforeTest
  public void before() {
    underTest = new ClassWrapperRenderer();
  }

  @Test
  public void testToMap() throws Exception {
    final Map<String, String> treeMap = underTest.toMap("a.b.c.d={D}\na.b.c.e={E}\na.b.c={C}\na.b={B}");

    //then
    assertEquals(treeMap.size(), 4, Joiner.on(", ").withKeyValueSeparator("=").join(treeMap));
    assertEquals(treeMap.get("a.b.c.d"), "{D}");
    assertEquals(treeMap.get("a.b.c.e"), "{E}");
    assertEquals(treeMap.get("a.b.c"), "{C}");
    assertEquals(treeMap.get("a.b"), "{B}");
  }

  @Test
  public void testAbbreviatePackage() throws Exception {

  }
}