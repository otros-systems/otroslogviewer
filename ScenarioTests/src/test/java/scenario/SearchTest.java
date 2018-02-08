package scenario;

import org.testng.annotations.Test;
import scenario.components.LogViewPanel;
import scenario.components.LogsTable;
import scenario.components.MainFrame;
import scenario.components.OpenPanel;
import scenario.components.WelcomeScreen;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;

import static org.awaitility.Awaitility.await;

public class SearchTest extends OtrosLogViewerBaseTest {

  @Test
  public void testSearchString() throws Exception {
    final int count = 12;

    final LogViewPanel logViewPanel = createFileAndImport(count, allInfo);

    final MainFrame mainFrame = new MainFrame(robot());
    mainFrame
      .setSearchModeByString()
      .enterSearchText("Message 1")
      .searchNext();
    final LogsTable logsTable = logViewPanel.logsTable();

    logsTable.waitForSelectedRow(1);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(10);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(11);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(1);
    mainFrame.searchPrevious();
    logsTable.waitForSelectedRow(11);
    mainFrame.searchPrevious();
    logsTable.waitForSelectedRow(10);

  }

  @Test
  public void testSearchRegex() throws Exception {
    final int count = 25;

    final LogViewPanel logViewPanel = createFileAndImport(count, allInfo);

    final MainFrame mainFrame = new MainFrame(robot());
    mainFrame
      .setSearchModeByRegex()
      .enterSearchText("Message.*3")
      .searchNext();
    final LogsTable logsTable = logViewPanel.logsTable();
    logsTable.waitForSelectedRow(3);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(13);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(23);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(3);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(13);
    mainFrame.searchPrevious();
    logsTable.waitForSelectedRow(3);
    mainFrame.searchPrevious();
    logsTable.waitForSelectedRow(23);
  }

  @Test
  public void testSearchQuery() throws Exception {
    final int count = 25;

    final Function<Integer, Level> every5Warning = integer -> {
      if (integer % 5 == 0) {
        return Level.WARNING;
      } else {
        return Level.INFO;
      }
    };

    final LogViewPanel logViewPanel = createFileAndImport(count, every5Warning);

    final MainFrame mainFrame = new MainFrame(robot());
    mainFrame
      .setSearchModeByQuery()
      .enterSearchText("LEVEL==WARNING")
      .searchNext();
    final LogsTable logsTable = logViewPanel.logsTable();
    logsTable.waitForSelectedRow(0);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(5);
    mainFrame.searchNext();
    logsTable.waitForSelectedRow(10);
    mainFrame.searchPrevious();
    logsTable.waitForSelectedRow(5);
    mainFrame.searchPrevious();
    logsTable.waitForSelectedRow(0);
  }


  private LogViewPanel createFileAndImport(int count, Function<Integer, Level> levelGenerator) throws IOException, InterruptedException {
    final File file = File.createTempFile("otrosTest", "");
    logEvents(file, count, levelGenerator);

    file.deleteOnExit();

    final MainFrame mainFrame = new MainFrame(robot());
    final OpenPanel openPanel = mainFrame
            .welcomeScreen()
            .waitFor()
            .clickOpenLogs();

    final LogViewPanel logViewPanel = openPanel
      .addFile(file)
      .importFiles();

    await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == count);
    return logViewPanel;
  }

}
