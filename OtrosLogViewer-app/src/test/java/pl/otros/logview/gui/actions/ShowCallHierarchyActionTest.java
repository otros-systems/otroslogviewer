package pl.otros.logview.gui.actions;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataBuilder;

public class ShowCallHierarchyActionTest {




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
