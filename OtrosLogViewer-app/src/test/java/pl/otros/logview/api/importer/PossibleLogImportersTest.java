package pl.otros.logview.api.importer;

import org.testng.annotations.Test;
import pl.otros.logview.importer.log4jxml.Log4jXmlLogImporter;
import pl.otros.logview.parser.json.log4j2.Log4j2JsonLogParser;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class PossibleLogImportersTest {

  @Test
  public void testAddMissing() {
    //given
    final PossibleLogImporters possibleLogImporters = new PossibleLogImporters();
    possibleLogImporters.getAvailableImporters().addAll(Arrays.asList(
      new Log4jXmlLogImporter()
    ));

    final List<LogImporter> toAdd = Arrays.asList(
      new Log4jXmlLogImporter(),
      new LogImporterUsingParser(new Log4j2JsonLogParser())
    );

    //when
    possibleLogImporters.addMissing(toAdd);


    //then
    assertEquals(possibleLogImporters.getAvailableImporters().size(),2);
  }
}