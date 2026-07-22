package pl.otros.logview.gui.session;

import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class SessionSerializer implements pl.otros.logview.api.services.Serializer<List<Session>, String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionSerializer.class.getName());

  @Override
  public String serialize(List<Session> sessions) {
    Collection<Session> withoutDuplicates = sessions.stream().collect(Collectors.toMap(Session::getName, Function.identity())).values();
    List<Map<String, Object>> sessionMaps = withoutDuplicates.stream().map(s -> {
      final Map<String, Object> map = new HashMap<>();
      map.put("name", s.getName());
      final List<Map<String, String>> files = s.getFilesToOpen().stream().map(f -> {
        final Map<String, String> map1 = new HashMap<>();
        map1.put("uri", f.getUri());
        map1.put("level", f.getLevel().getName());
        map1.put("openMode", f.getOpenMode().name());
        f.getLogImporter().ifPresent(l -> {
          map1.put("logImporter", l);
        });
        return map1;
      }).collect(Collectors.toList());
      map.put("filesToOpen", files);
      return map;
    }).collect(toList());
    final String result = new GsonBuilder().setPrettyPrinting().create().toJson(sessionMaps);
    LOGGER.info("Serialized session: {}", result);
    return result;
  }
}
