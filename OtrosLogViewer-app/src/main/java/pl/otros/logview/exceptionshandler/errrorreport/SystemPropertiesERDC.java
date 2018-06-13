package pl.otros.logview.exceptionshandler.errrorreport;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SystemPropertiesERDC implements ErrorReportDataCollector {

  static final String PREFIX = "SYSTEM_PROPERTIES:";

  @Override
  public Map<String, String> collect(ErrorReportCollectingContext context) {
    Map<String, String> r = new HashMap<>();
    Properties properties = System.getProperties();
    fillValues(r, properties);
    return r;
  }

  void fillValues(Map<String, String> map, Properties properties) {
    for (Object keyObject : properties.keySet()) {
      String key = keyObject.toString();
      String property = properties.getProperty(key);
      if (!key.toLowerCase().matches(".*(user\\.|password).*")) {
        map.put(PREFIX + key, property);
      }
    }

  }

}
