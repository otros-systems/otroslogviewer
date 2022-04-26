package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.core.Robot;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.dependency.jsr305.Nonnull;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class OpenPanel extends TestComponent<JPanelFixture, OpenPanel> {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenPanel.class);
  private final FrameFixture frame;

  public OpenPanel(FrameFixture frameFixture, Robot robot) {
    super(robot);
    this.frame = frameFixture;
  }

  public OpenPanel addFile(File file) {
    me().button("OpenPanel.add more files").click();
    final DialogFixture vfsBrowserDialog = WindowFinder.findDialog("VfsBrowserDialog").using(robot);
    openFile(file, vfsBrowserDialog);
    LOGGER.info("File " + file.getAbsolutePath() + " added to open list");
    return this;
  }

  public LogViewPanel importFiles() {
    LOGGER.info("Clicking import logs");
    await().ignoreExceptions().until(() -> frame.button("OpenPanel.import").click());
    return new LogViewPanel(frame, robot);
  }

  private static void openFile(File file, DialogFixture vfsBrowserDialog) {
    LOGGER.info("Open file " + file.getAbsolutePath()
    );
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
        LOGGER.info("Checking: {} {}", component.getName(), component.getRowCount());
        return true;
      }
    });
    while (table.cell(TableCell.row(0).column(0)).value().equals("[..]")) {
      LOGGER.info("Clicking on [..]");
      table.cell("[..]").click();
      table.pressAndReleaseKey(KeyPressInfo.keyCode('\n'));
      await().atLeast(250L, TimeUnit.MILLISECONDS);
    }
    while (paths.size() > 0) {
      final String dir = paths.removeLast().getName();
      LOGGER.info("Will open " + dir);
      if (dir.length() > 0) {
        await().ignoreExceptions().until(() -> {
          if (dir.equalsIgnoreCase("appdata") && System.getProperty("os.name").toLowerCase().contains("win")) {
            //AppData folder is not visible when listing files in user home :(
            JTextComponentFixture pathTextBox = vfsBrowserDialog.textBox("VfsBrowser.path");
            pathTextBox.setText(pathTextBox.text() + "/" + dir);
            pathTextBox.pressAndReleaseKey(KeyPressInfo.keyCode('\n'));
          } else {
            LOGGER.info(" clicking on " + dir);
            final JTableCellFixture cell = table.cell(dir);
            LOGGER.info(" table cell: " + cell.value());
            cell.doubleClick();
          }
        });
      }
    }
    final String name = file.getName();
    LOGGER.info(" opening file " + name);
    vfsBrowserDialog.textBox("VfsBrowser.filter").setText(name);
    vfsBrowserDialog.button("VfsBrowser.refresh").click();
    await().ignoreExceptions().until(() -> table.cell(TableCell.row(1).column(0)).click());
    vfsBrowserDialog.button("VfsBrowser.open").click();
    LOGGER.info(" file " + file.getAbsolutePath() + " opened");
  }

  @Override
  public JPanelFixture me() {
    return frame.panel("OpenPanel");
  }
}
