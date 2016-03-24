package pl.otros.logview.store.async;

import org.testng.Assert;
import org.testng.annotations.Test;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.LogDataBuilder;

import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;

@Test(enabled=false)
public class MemoryLogDataStore2Test {
  @Test
  public void testFilter() throws Exception {
    //given
    MemoryLogDataStore2 logDataStore2 = new MemoryLogDataStore2();
    String[] messages = "Ala ma kota a kot ma ale".split(" ");
    LogData[] logDatas = new LogData[10000*messages.length];
    for (int i=0; i<logDatas.length;i++){
      LogData logData = new LogDataBuilder().withDate(new Date(100000l + i * 100)).withMessage(messages[i % messages.length]).build();
      logDatas[i]=logData;
    }
    logDataStore2.add(logDatas);

    //when
    logDataStore2.filter(new LogDataFilter("kot"));

    //then
    int filteredCount = logDataStore2.getCount();
    assertEquals(20000,filteredCount);
  }

  
  public void testSearch() throws Exception {
    Assert.fail("No implemented");
  }
}
