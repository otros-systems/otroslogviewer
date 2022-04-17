package pl.otros.logview.api.importer;

import org.testng.annotations.Test;
import pl.otros.logview.importer.UtilLoggingXmlLogImporter;
import pl.otros.logview.parser.json.log4j2.Log4j2JsonLogParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class PossibleLogImportersTest {

  @Test
  public void testAddMissing() {
    //given
    final PossibleLogImporters possibleLogImporters = new PossibleLogImporters();
    possibleLogImporters.getAvailableImporters().addAll(Collections.singletonList(new UtilLoggingXmlLogImporter()));

    final List<LogImporter> toAdd = Arrays.asList(
      new UtilLoggingXmlLogImporter(),
      new LogImporterUsingParser(new Log4j2JsonLogParser())
    );

    //when
    possibleLogImporters.addMissing(toAdd);


    //then
    assertEquals(possibleLogImporters.getAvailableImporters().size(),2);
  }
}