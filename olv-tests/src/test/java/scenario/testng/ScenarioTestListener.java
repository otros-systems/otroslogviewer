package scenario.testng;

import com.google.common.base.Throwables;
import org.assertj.swing.testng.listener.AbstractTestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.util.stream.Collectors;

public class ScenarioTestListener extends AbstractTestListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioTestListener.class);

  @Override
  public void onFinish(ITestContext context) {
    final String failed = format(context.getFailedTests());
    final String successful = format(context.getPassedTests());
    LOGGER.info("Done\nSuccessful:\n" + successful +
      "\n\nFailed tests:\n" + failed +
      "\n\nSkipped (or retried):\n" + format(context.getSkippedTests()));
  }

  private String format(IResultMap skippedTests) {
    return skippedTests.getAllResults()
      .stream()
      .map(r -> " * " + r.getTestClass().getName() + "." + r.getName())
      .sorted()
      .collect(Collectors.joining("\n"));
  }

  @Override
  public void onTestFailure(ITestResult result) {
    final String stacktrace = Throwables.getStackTraceAsString(result.getThrowable());
    LOGGER.error("Failed " + result.getName(), stacktrace);

  }
}
