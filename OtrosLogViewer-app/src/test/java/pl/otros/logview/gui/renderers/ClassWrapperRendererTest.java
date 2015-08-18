package pl.otros.logview.gui.renderers;

import com.google.common.base.Joiner;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.SortedMap;

import static org.testng.Assert.assertEquals;

public class ClassWrapperRendererTest {

  private ClassWrapperRenderer underTest;

  @BeforeTest
  public void before() {
    underTest = new ClassWrapperRenderer();
  }

  @Test
  public void testAbbreviatePackageUsingMappings(){
    //given
    SortedMap<String, String> map = underTest.toMap("com.company={D}\ncom={E}\ncom.company.package={C}");

    //when
    final String actual = underTest.abbreviatePackageUsingMappings("com.company.package.Class", map);

    //then
    assertEquals(actual,"{C}.Class");
  }
  @Test
  public void testAbbreviatePackageUsingMappingsMappingNotFound(){
    //given
    SortedMap<String, String> map = underTest.toMap("com.company={D}\ncom={E}\ncom.company.package={C}");

    //when
    final String actual = underTest.abbreviatePackageUsingMappings("co.company.package.Class", map);

    //then
    assertEquals(actual,"co.company.package.Class");
  }

  @Test
  public void testToMap() throws Exception {
    final SortedMap<String, String> treeMap = underTest.toMap("a.b.c.d={D}\na.b.c.e={E}\na.b.c={C}\na.b={B}");

    //then
    assertEquals(treeMap.size(), 4, Joiner.on(", ").withKeyValueSeparator("=").join(treeMap));
    assertEquals(treeMap.get("a.b.c.d"), "{D}");
    assertEquals(treeMap.get("a.b.c.e"), "{E}");
    assertEquals(treeMap.get("a.b.c"), "{C}");
    assertEquals(treeMap.get("a.b"), "{B}");
  }

  @Test
  public void testToMapIfConfIsNull() throws Exception {
    final Map<String, String> treeMap = underTest.toMap(null);

    //then
    assertEquals(treeMap.size(), 0);
  }

  @Test
  public void testIsNullSafe() throws Exception {
    underTest.getTableCellRendererComponent(Mockito.mock(JTable.class), null, false, false, 0, 0);
  }

  @DataProvider(name = "abbreviations")
  public Object[][] abbreviations(){
    return new Object[][] {
        new Object[]{"com.company.package.Class",26,"com.company.package.Class"},
        new Object[]{"com.company.package.Class",25,"com.company.package.Class"},
        new Object[]{"com.company.package.Class",24,"c.company.package.Class"},
        new Object[]{"com.company.package.Class",13,"c.c.p.Class"},
        new Object[]{"com.company.package.Class",11,"c.c.p.Class"},
        new Object[]{"com.company.package.Class",5,"c.c.p.Class"},
        new Object[]{"Class",8,"Class"},
        new Object[]{"Class",3,"Class"},
        new Object[]{"",3,""},
        new Object[]{"com.package.Dot.",1,"c.p.D."},
        new Object[]{"com.package.Dot.",9,"c.p.Dot."},
        new Object[]{".com.package.Dot.",10,".c.p.Dot."},
    };
  }

  @Test(dataProvider= "abbreviations")
  public void testAbbreviatePackagesToSingleLetter(String clazz,int width, String expected) {
    //given
    final FontMetrics fontMetrics = Mockito.mock(FontMetrics.class);
    Mockito.when(fontMetrics.stringWidth(Matchers.anyString())).thenAnswer(invocation -> ((String)invocation.getArguments()[0]).length());

    //when
    final String actual = underTest.abbreviatePackagesToSingleLetter(clazz, width, fontMetrics);

    //then
    assertEquals(actual,expected);
  }
}