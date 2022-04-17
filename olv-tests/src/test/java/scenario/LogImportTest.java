package scenario;

import org.awaitility.Awaitility;
import org.testng.annotations.Test;
import scenario.components.ConfirmClose;
import scenario.components.LogViewPanel;
import scenario.components.MainFrame;
import scenario.components.OpenPanel;
import scenario.testng.RetryAnalyzer;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 * Reaping keys on mac
 * <p>
 * http://osxdaily.com/2011/08/04/enable-key-repeat-mac-os-x-lion/
 * defaults write -g ApplePressAndHoldEnabled -bool false
 * <p>
 * LogViewMainFrame:228 to work faster have to comment this line:
 * //    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueueProxy());
 */
public class LogImportTest extends OtrosLogViewerBaseTest {

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void testImport1File() throws Exception {

    final File file = File.createTempFile("otrosTest", "");
    logEvents(file, 10_000);

    final MainFrame mainFrame = new MainFrame(robot());
    final OpenPanel openPanel = mainFrame.welcomeScreen().clickMergeLogs();

    final LogViewPanel logViewPanel = openPanel
      .addFile(file)
      .importFiles();

    Awaitility.await()
      .atMost(30, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 10_000);

    mainFrame.tabBar().tab().close();
    ConfirmClose.close(robot());

  }

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void testImport2Files() throws Exception {
    final File file1 = File.createTempFile("otrosTest", "");
    logEvents(file1, 10);
    final File file2 = File.createTempFile("otrosTest", "");
    logEvents(file1, 10);

    final MainFrame mainFrame = new MainFrame(robot());
    final OpenPanel openPanel = mainFrame.welcomeScreen().clickMergeLogs();

    final LogViewPanel logViewPanel = openPanel
      .addFile(file1)
      .addFile(file2)
      .importFiles();

    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 20);

  }

}
