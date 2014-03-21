package pl.otros.logview.gui.actions;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.gui.LogDataTableModel;

import java.util.ArrayList;

public class ShowCallHierarchyActionTest {


  @Test(enabled=false)
  public void testFindCallHierarchyEvents() {
    ShowCallHierarchyAction callHierarchyAction = new ShowCallHierarchyAction(null,null,null);
    LogDataTableModel model = new LogDataTableModel();
    ArrayList<Integer> listOfEvents2 = new ArrayList<Integer>();
    ArrayList<Integer> listEntryEvents = new ArrayList<Integer>();
    int selected = 4;
    Assert.fail("not implemented");

  }

  @Test
  public void testTheSameLogMethod() {
    ShowCallHierarchyAction action = new ShowCallHierarchyAction(null, null, null);
    LogData ld1 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    LogData ld2 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    AssertJUnit.assertTrue(action.theSameLogMethod(ld1, ld2));
  }

  @Test
  public void testTheSameLogMethodDifferentClass() {
    ShowCallHierarchyAction action = new ShowCallHierarchyAction(null, null, null);
    LogData ld1 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    LogData ld2 = new LogDataBuilder().withId(1).withClass("a.b.d").withMethod("a").build();
    AssertJUnit.assertFalse(action.theSameLogMethod(ld1, ld2));
  }

  @Test
  public void testTheSameLogMethodDifferentMethod() {
    ShowCallHierarchyAction action = new ShowCallHierarchyAction(null, null, null);
    LogData ld1 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    LogData ld2 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("b").build();
    AssertJUnit.assertFalse(action.theSameLogMethod(ld1, ld2));
  }
}
