package pl.otros.logview.gui.actions;

import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.gui.LogDataTableModel;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ShowCallHierarchyActionTest {

  //@Test
  public void testFindCallHierarchyEvents() {
    ShowCallHierarchyAction callHierarchyAction = new ShowCallHierarchyAction(null,null,null);
    LogDataTableModel model = new LogDataTableModel();
    ArrayList<Integer> listOfEvents2 = new ArrayList<Integer>();
    ArrayList<Integer> listEntryEvents = new ArrayList<Integer>();
    int selected = 4;
    fail("not implemented");

  }

  @Test
  public void testTheSameLogMethod() {
    ShowCallHierarchyAction action = new ShowCallHierarchyAction(null, null, null);
    LogData ld1 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    LogData ld2 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    assertTrue(action.theSameLogMethod(ld1, ld2));
  }

  @Test
  public void testTheSameLogMethodDifferentClass() {
    ShowCallHierarchyAction action = new ShowCallHierarchyAction(null, null, null);
    LogData ld1 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    LogData ld2 = new LogDataBuilder().withId(1).withClass("a.b.d").withMethod("a").build();
    assertFalse(action.theSameLogMethod(ld1, ld2));
  }

  @Test
  public void testTheSameLogMethodDifferentMethod() {
    ShowCallHierarchyAction action = new ShowCallHierarchyAction(null, null, null);
    LogData ld1 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("a").build();
    LogData ld2 = new LogDataBuilder().withId(1).withClass("a.b.c").withMethod("b").build();
    assertFalse(action.theSameLogMethod(ld1, ld2));
  }
}
