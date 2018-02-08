package scenario;

import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.testng.testcase.AssertJSwingTestngTestCase;
import pl.otros.logview.gui.LogViewMainFrame;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class OtrosLogViewerBaseTest extends AssertJSwingTestngTestCase {
  @Override
  protected void onSetUp() {
    System.setProperty("runForScenarioTest", "true");

    ApplicationLauncher.application(LogViewMainFrame.class).start();
  }

  protected Function<Integer, Level> allInfo = integer -> Level.INFO;

  public void logEvents(File file, int count) throws IOException {
    logEvents(file, count, allInfo);
  }

  public void logEvents(File file, int count, Function<Integer, Level> levelGenerator) throws IOException {
    final Logger logger = Logger.getLogger("some logger");
    logger.setUseParentHandlers(false);
    logger.addHandler(new FileHandler(file.getAbsolutePath()));
    IntStream
      .range(0, count)
      .forEach(i -> logger.log(levelGenerator.apply(i), "Message " + i));
  }

}
