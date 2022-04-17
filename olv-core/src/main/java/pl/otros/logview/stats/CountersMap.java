package pl.otros.logview.stats;

import com.google.common.base.Splitter;
import pl.otros.logview.api.services.Deserializer;
import pl.otros.logview.api.services.Serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CountersMap {

  public static final String SPERATOR = ";";
  private Map<String, Long> map = new HashMap<>();


  public CountersMap() {
  }

  public CountersMap(Map<String, Long> map) {
    this.map = map;
  }

  public Map<String, Long> getMap() {
    return map;
  }

  public void setMap(Map<String, Long> map) {
    this.map = map;
  }

  public static Deserializer<CountersMap, String> deserializer() {
    return (String value) -> {
      final Map<String, String> split = Splitter.on("\n").withKeyValueSeparator(SPERATOR).split(value);
      final Map<String, Long> collect = split
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> new Long(entry.getValue())));
      return Optional.of(new CountersMap(collect));
    };
  }

  public static Serializer<CountersMap, String> serializer() {
    return value -> value.getMap().entrySet().stream().map(entry -> entry.getKey() + SPERATOR + entry.getValue()).collect(Collectors.joining("\n"));
  }
}
