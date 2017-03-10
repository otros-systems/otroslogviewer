package scenario;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.dependency.jsr305.Nonnull;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.testng.testcase.AssertJSwingTestngTestCase;
import org.testng.annotations.Test;
import pl.otros.logview.gui.LogViewMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;

import static org.assertj.swing.finder.WindowFinder.findDialog;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

public class ExampleUiTest
  extends AssertJSwingTestngTestCase {

  @Test
  public void testMainScreen() throws Exception {
    robot().settings().eventPostingDelay(20);
    robot().settings().delayBetweenEvents(20);

    robot().settings().simpleWaitForIdle(false);

    FrameFixture frame = findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
      protected boolean isMatching(JFrame frame) {
        return frame.getTitle().startsWith("OtrosLogViewer") && frame.isShowing();
      }
    }).using(robot());

    final JButtonFixture openLogs = frame.button("open log files");
    System.out.println("Clicking");
    openLogs.click();
    System.out.println("Clicked");
    Thread.sleep(1000);
    frame.button("add more files").click();
    //"VfsBrowser.path"
    final File tempFile = File.createTempFile("otrosTest","");
    final FileWriter fileWriter = new FileWriter(tempFile);
    fileWriter.write("Oct 9, 2010 12:46:34 PM log.test.LogTest main\n" +
      "FINEST: Message in locales en_US 1\n" +
      "Oct 9, 2010 12:46:34 PM log.test.LogTest main\n" +
      "FINER: Message in locales en_US 2\n");
    fileWriter.close();

    findDialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
      @Override
      protected boolean isMatching(@Nonnull Dialog component) {
        System.out.println("Searching for dialog " + component.getTitle() + " / " + component.getName());
        return false;
      }
    });
    final DialogFixture vfsBrowserDialog = findDialog("VfsBrowserDialog").using(robot());
    vfsBrowserDialog
      .textBox("VfsBrowser.path")
      .deleteText()
      .enterText(tempFile.getAbsolutePath())
      .pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ENTER));

//    vfsBrowserDialog.button("VfsBrowser.open").click();


    System.out.println("Sleeping");
    Thread.sleep(10000);

  }

  @Override
  protected void onSetUp() {
    application(LogViewMainFrame.class).start();
  }
}
