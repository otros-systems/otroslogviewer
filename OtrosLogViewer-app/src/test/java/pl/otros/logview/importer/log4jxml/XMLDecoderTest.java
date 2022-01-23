package pl.otros.logview.importer.log4jxml;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import pl.otros.logview.parser.log4j.LoggingEvent;

import java.util.Vector;

public class XMLDecoderTest {
  private static final String EVENT_START = "<log4j:event logger=\"package.Class\" timestamp=\"1434301820356\" level=\"ERROR\" thread=\"MyThread\">";
  private static final String EVENT_LOCATION = "<log4j:locationInfo class=\"package.Class\" method=\"main\" file=\"Class.java\" line=\"1\"/>";
  private static final String EVENT_END = "</log4j:event>";
  private static final String CONTENT_WITH_CDATA = " >>PREFIX<< <![CDATA[ >>CONTENT<< ]]> >>SUFFIX<< ";

  @Test
  public void testNestedCData() {
    XMLDecoder xmlDecoder = new XMLDecoder();
    String log4jEvent = EVENT_START;
    for (String tag : new String[] { "log4j:message", "log4j:NDC", "log4j:MDC", "log4j:throwable" }) {
      log4jEvent = log4jEvent + String.format("<%1$s><![CDATA[%2$s]]></%1$s>", tag, CONTENT_WITH_CDATA);
    }
    log4jEvent = log4jEvent + EVENT_LOCATION + EVENT_END;

    Vector<LoggingEvent> events = xmlDecoder.decodeEvents(log4jEvent + log4jEvent);

    assertEquals(events.size(), 2);
    for (LoggingEvent event : events) {
      assertEquals(event.getMessage(), CONTENT_WITH_CDATA);
      assertEquals(event.getNdc(), CONTENT_WITH_CDATA);
      assertEquals(event.getProperties().get(CONTENT_WITH_CDATA), "");
      assertEquals(event.getThrowable()[0], CONTENT_WITH_CDATA);
    }
  }
}
