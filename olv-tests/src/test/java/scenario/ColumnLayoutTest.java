package scenario;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPopupMenuFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.awaitility.Awaitility;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.pluginable.AllPluginables;
import scenario.components.LogViewPanel;
import scenario.components.MainFrame;
import scenario.testng.RetryAnalyzer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ColumnLayoutTest extends OtrosLogViewerBaseTest {

  @BeforeTest
  public static void beforeTest() {
    //Delete configuration to have a clean start
    try {
      MoreFiles.deleteRecursively(
        AllPluginables.USER_CONFIGURATION_DIRECTORY.toPath(),
        RecursiveDeleteOption.ALLOW_INSECURE
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void testSaveAndReloadColumnLayout() throws IOException {
    final MainFrame mainFrame = new MainFrame(robot());

    // 1. Prepare log events and open file
    final File file = File.createTempFile("columnLayoutTest", ".log");
    file.deleteOnExit();
    // We use a format that OtrosLogViewer recognizes reliably.
    // logEvents writes standard JUL XML, which OtrosLogViewer can parse.
    logEvents(file, 10);

    final LogViewPanel logViewPanel = mainFrame.welcomeScreen()
      .clickOpenFile()
      .openFile(file);

    // Wait until data is loaded
    Awaitility.await()
      .atMost(15, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() >= 10);

    JTableFixture table = logViewPanel.logsTable().me();

    // 2. Check current columns
    // Click the named button
    logViewPanel.me().button("LogViewPanel.columnControlButton").click();

    // 3. Hide column (e.g. "ID")
    // We look for the popup menu.
    JPopupMenuFixture popupMenu = new JPopupMenuFixture(robot(), robot().findActivePopupMenu());
    popupMenu.menuItemWithPath(TableColumns.ID.getName()).click();

    Awaitility.await()
      .ignoreExceptions()
      .atMost(10, TimeUnit.SECONDS).until(() -> table.target().convertColumnIndexToView(TableColumns.ID.getColumn()) == -1);

    // 4. Save layout
    // Open menu again
    logViewPanel.me().button("LogViewPanel.columnControlButton").click();

    popupMenu = new JPopupMenuFixture(robot(), robot().findActivePopupMenu());
    popupMenu.menuItemWithPath("Save current column layout").click();

    // Enter name in JOptionPane
    FrameFixture mainFrameFixture = mainFrame.me();
    mainFrameFixture.optionPane().textBox().enterText("TestLayout");
    mainFrameFixture.optionPane().okButton().click();

    // 5. Verify
    // We check if the new layout appears in the menu
    logViewPanel.me().button("LogViewPanel.columnControlButton").click();
    popupMenu = new JPopupMenuFixture(robot(), robot().findActivePopupMenu());
    popupMenu.menuItem(new GenericTypeMatcher<>(JMenuItem.class) {
      @Override
      protected boolean isMatching(JMenuItem component) {
        return component.getText().contains("TestLayout");
      }
    }).requireVisible();

    // We do a reset to default
    popupMenu.menuItemWithPath("Reset to default layout").click();

    // Now ID should be back again
    Awaitility.await()
      .ignoreExceptions()
      .atMost(10, TimeUnit.SECONDS).until(() -> table.target().convertColumnIndexToView(TableColumns.ID.getColumn()) == 0);

    // Now apply the saved layout
    logViewPanel.me().button("LogViewPanel.columnControlButton").click();
    popupMenu = new JPopupMenuFixture(robot(), robot().findActivePopupMenu());
    popupMenu.menuItemWithPath("Switch layout to \"TestLayout\"").click();

    // Now ID should be gone again
    Awaitility.await()
      .ignoreExceptions()
      .atMost(10, TimeUnit.SECONDS).until(() -> table.target().convertColumnIndexToView(TableColumns.ID.getColumn()) == -1);
  }
}
