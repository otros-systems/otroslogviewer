package pl.otros.logview.parser;


import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

public class CustomLevelsParser {

  private final Map<String, Level> customLevelDefinitionMap;

  public CustomLevelsParser(String customLevelDefinitions) {
    customLevelDefinitionMap = updateCustomLevelDefinitionMap(customLevelDefinitions);
  }

  public Optional<Level> parse(String levelString) {
    if (customLevelDefinitionMap.containsKey(levelString)) {
      return Optional.of(customLevelDefinitionMap.get(levelString));
    } else {
      return Optional.empty();
    }
  }

  private Map<String, Level> updateCustomLevelDefinitionMap(String customLevelDefinitions) {
    final Map<String, Level> customLevelDefinitionMap = new HashMap<>();

    if (customLevelDefinitions != null) {
      StringTokenizer entryTokenizer = new StringTokenizer(customLevelDefinitions, ",");

      customLevelDefinitionMap.clear();
      while (entryTokenizer.hasMoreTokens()) {
        StringTokenizer innerTokenizer = new StringTokenizer(entryTokenizer.nextToken(), "=");
        String key = innerTokenizer.nextToken();
        String value = innerTokenizer.nextToken();
        customLevelDefinitionMap.put(key, Level.toLevel(value));
      }
    }
    return customLevelDefinitionMap;
  }
}
