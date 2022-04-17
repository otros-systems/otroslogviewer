package pl.otros.logview.gui.session;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class SessionSerializer implements pl.otros.logview.api.services.Serializer<List<Session>, String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionSerializer.class.getName());

  @Override
  public String serialize(List<Session> sessions) {
    Collection<Session> withoutDuplicates = sessions.stream().collect(Collectors.toMap(Session::getName, Function.identity())).values();
    List<JSONObject> objects = withoutDuplicates.stream().map(s -> {
      final HashMap<String, Object> map = new HashMap<>();
      map.put("name", s.getName());
      final List<JSONObject> files = s.getFilesToOpen().stream().map(f -> {
        final HashMap<String, String> map1 = new HashMap<>();
        map1.put("uri", f.getUri());
        map1.put("level", f.getLevel().getName());
        map1.put("openMode", f.getOpenMode().name());
        f.getLogImporter().ifPresent(l -> {
          map1.put("logImporter", l);
        });
        return new JSONObject(map1);
      }).collect(Collectors.toList());
      final JSONObject jsonObject = new JSONObject(map);
      try {
        return jsonObject.accumulate("filesToOpen", new JSONArray(files));
      } catch (JSONException e) {
        LOGGER.error("Can't serialize: ", e);
      }
      return jsonObject;
    }).collect(toList());
    final String result = new JSONArray(objects).toString();
    LOGGER.info("Serialized session: {}", result);
    return result;
  }
}
