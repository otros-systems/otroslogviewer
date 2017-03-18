package scenario;

import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.testng.testcase.AssertJSwingTestngTestCase;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;
import pl.otros.logview.gui.LogViewMainFrame;
import scenario.components.ConfirmClose;
import scenario.components.LogViewPanel;
import scenario.components.MainFrame;
import scenario.components.OpenPanel;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class ExampleUiTest extends AssertJSwingTestngTestCase {

  @Test
  public void testBasicImport() throws Exception {

    final File tempFile1 = File.createTempFile("otrosTest", "");
    final File tempFile2 = File.createTempFile("otrosTest", "");
    writeToFile(tempFile1);
    writeToFile(tempFile2);

    final MainFrame mainFrame = new MainFrame(robot());
    final OpenPanel openPanel = mainFrame.welcomeScreen().clickOpenLogs();

    final LogViewPanel logViewPanel = openPanel.addFile(tempFile1)
      .addFile(tempFile2)
      .importFiles();

    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 4);

    mainFrame.tabBar().tab().close();

    ConfirmClose.close(robot());

    mainFrame.welcomeScreen().waitFor();
  }


  private void writeToFile(File tempFile) throws IOException {
    final FileWriter fileWriter = new FileWriter(tempFile);
    fileWriter.write("Oct 9, 2010 12:46:34 PM log.test.LogTest main\n" +
      "FINEST: Message in locales en_US 1\n" +
      "Oct 9, 2010 12:46:34 PM log.test.LogTest main\n" +
      "FINER: Message in locales en_US 2\n");
    fileWriter.close();
  }


  @Override
  protected void onSetUp() {
    System.setProperty("runForScenarioTest", "true");

    ApplicationLauncher.application(LogViewMainFrame.class).start();
  }

}
