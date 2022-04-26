package scenario;

import org.awaitility.Awaitility;
import org.testng.annotations.Test;
import scenario.components.ConfirmClose;
import scenario.components.LogViewPanel;
import scenario.components.MainFrame;
import scenario.components.OpenLogFileDialog;
import scenario.components.filter.LevelFilterPanel;
import scenario.testng.RetryAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/*
Call hierarchy
Class Filter
Level filter
Logger Filter
Mark/Note filter
Property filter
Regex filter
String contains filter
Thread Filter
Time filter
 */
public class FilterTest extends OtrosLogViewerBaseTest {

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void testLevelFilter() throws IOException {
    final File file = File.createTempFile("otrosTest", "");
    file.deleteOnExit();
    logEvents(file, 10_000, i -> {
      if (i % 2 == 0) {
        return Level.INFO;
      } else if (i % 3 == 0) {
        return Level.WARNING;
      }
      return Level.FINE;
    });

    final MainFrame mainFrame = new MainFrame(robot());
    OpenLogFileDialog openLogFileDialog = mainFrame.welcomeScreen().clickOpenFile();
    LogViewPanel logViewPanel = openLogFileDialog.openFile(file);

    Awaitility.await()
      .atMost(30, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 10_000);

    LevelFilterPanel levelFilterPanel = logViewPanel.filterPanel().levelFilterPanel();
    levelFilterPanel.checkBox().click();
    levelFilterPanel.selectLevel(Level.WARNING);

    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 1_667);

    mainFrame.tabBar().tab().close();
    ConfirmClose.close(robot());

  }
}
