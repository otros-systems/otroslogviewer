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
    System.setProperty(LogViewMainFrame.RUN_FOR_SCENARIO_TEST, "true");

    ApplicationLauncher.application(LogViewMainFrame.class).start();
  }

  protected Function<Integer, Level> allInfo = integer -> Level.INFO;

  public Logger logEvents(File file, int count) throws IOException {
    return logEvents(file, count, allInfo);
  }

  public Logger logEvents(File file, int count, Function<Integer, Level> levelGenerator) throws IOException {
    return logEvents(file, count, levelGenerator, i -> "Message " + i);
  }


  public Logger logEvents(File file, int count, Function<Integer, Level> levelGenerator, Function<Integer, String> messageGenerator) throws IOException {
    final Logger logger = Logger.getLogger("some logger");
    logger.setUseParentHandlers(false);
    logger.setLevel(Level.FINEST);
    logger.addHandler(new FileHandler(file.getAbsolutePath()));
    IntStream
      .range(0, count)
      .forEach(i -> logger.log(levelGenerator.apply(i), messageGenerator.apply(i)));
    return logger;
  }


}
