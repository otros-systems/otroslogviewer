package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.dependency.jsr305.Nonnull;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class OpenPanel extends TestComponent<JPanelFixture, OpenPanel> {

  private FrameFixture frame;

  OpenPanel(FrameFixture frameFixture, Robot robot) {
    super(robot);
    this.frame = frameFixture;
  }

  public OpenPanel addFile(File file) throws InterruptedException {
    me().button("OpenPanel.add more files").click();
    WindowFinder.findDialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
      @Override
      protected boolean isMatching(@Nonnull Dialog component) {
        System.out.println("Searching for dialog " + component.getTitle() + " / " + component.getName());
        return false;
      }
    });
    final DialogFixture vfsBrowserDialog = WindowFinder.findDialog("VfsBrowserDialog").using(robot);

    openFile(file, vfsBrowserDialog);
//    Thread.sleep(1000);
    return this;
  }

  public LogViewPanel importFiles() {
    frame.button("OpenPanel.import").click();
    return new LogViewPanel(frame, robot);
  }

  private static void openFile(File file, DialogFixture vfsBrowserDialog) throws InterruptedException {
    LinkedList<File> paths = new LinkedList<>();
    File parentFile = file;
    while ((parentFile = parentFile.getParentFile()) != null) {
      paths.add(parentFile);
    }

    vfsBrowserDialog.textBox("VfsBrowser.filter").setText("");
    vfsBrowserDialog.textBox("VfsBrowser.path").click();


    final JTableFixture table = vfsBrowserDialog.table(new GenericTypeMatcher<JTable>(JTable.class) {
      @Override
      protected boolean isMatching(@Nonnull JTable component) {
        System.out.printf("Checking: %s %s %n", component.getName(), component.getRowCount());
        return true;
      }
    });
    await().atMost(5, TimeUnit.SECONDS).until(() -> table.target().isVisible());


    while (table.cell(TableCell.row(0).column(0)).value().equals("[..]")) {
      table.cell("[..]").doubleClick();
      Thread.sleep(50);
    }
    while (paths.size() > 0) {
      final String dir = paths.removeLast().getName();
      if (dir.length() > 0) {
        await().until(() -> table.cell(dir).doubleClick());
      }
    }
    final String name = file.getName();
    vfsBrowserDialog.textBox("VfsBrowser.filter").setText(name);
    vfsBrowserDialog.textBox("VfsBrowser.path").click();
    final JTableCellFixture cell = table.cell(TableCell.row(1).column(0));
    cell.click();
    vfsBrowserDialog.button("VfsBrowser.open").click();

  }

  @Override
  public JPanelFixture me() {
    return frame.panel("OpenPanel");
  }
}
