package scenario.testng;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

  private int counter = 0;
  private int retryLimit = 2;

  @Override
  public boolean retry(ITestResult result) {
    return counter++ < retryLimit;
  }
}
