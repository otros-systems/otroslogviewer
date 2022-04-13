package pl.otros.logview.api.model;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

public class LocationInfoTest {

  @DataProvider(name = "removeLambdas")
  public Object[][] removeLambdasDataProvider() {
    return new Object[][]{
      {"\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)", "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)"},
      {"at test.sampleapp.SampleAppMultiThreadedFix2.lambda$performRequests$16(SampleAppMultiThreadedFix2.java:36)", "at test.sampleapp.SampleAppMultiThreadedFix2.lambda$performRequests$16(SampleAppMultiThreadedFix2.java:36)"},
      {"at test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$16/2016207428.accept(Unknown Source)", "at test.sampleapp.SampleAppMultiThreadedFix2$Lambda.accept(Unknown Source)"},
    };
  }


  @Test(dataProvider = "removeLambdas")
  public void testRemoveLambdas(String stacktraceLine,String expected){
    Assert.assertEquals(LocationInfo.removeLambdas(stacktraceLine),expected);
  }

  @DataProvider(name = "parse")
  public Object[][] parseDataProvider() {
    return new Object[][]{
      {"\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)", new LocationInfo("java.util.concurrent", "java.util.concurrent.FutureTask", "report", "FutureTask.java", Optional.of(122), null)},
      {"at test.sampleapp.SampleAppMultiThreadedFix2.lambda$performRequests$16(SampleAppMultiThreadedFix2.java:36)", new LocationInfo("test.sampleapp", "test.sampleapp.SampleAppMultiThreadedFix2", "lambda$performRequests$16", "SampleAppMultiThreadedFix2.java", Optional.of(36), null)},
      {"at test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$16/2016207428.accept(Unknown Source)", null},
      {"at test.sampleapp.services.hotels.HotelsService.getHotels(HotelsService.java:30)", new LocationInfo("test.sampleapp.services.hotels", "test.sampleapp.services.hotels.HotelsService", "getHotels", "HotelsService.java", Optional.of(30), null)},
    };
  }


  @Test(dataProvider = "parse")
  public void testParse(String s, LocationInfo expected) throws Exception {
    //when
    final LocationInfo actual = LocationInfo.parse(s);

    //then
    Assert.assertEquals(actual, expected);

  }
}