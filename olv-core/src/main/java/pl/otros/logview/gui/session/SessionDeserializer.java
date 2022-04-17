package pl.otros.logview.gui.session;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.services.Deserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class SessionDeserializer implements Deserializer<List<Session>, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionDeserializer.class.getName());

  @Override
  public Optional<List<Session>> deserialize(String data) {
    final ArrayList<Session> sessions = new ArrayList<>();
    try {
      final JSONArray array = new JSONArray(data);
      for (int i = 0; i < array.length(); i++) {
        final JSONObject jsonObject = array.getJSONObject(i);
        String name = jsonObject.getString("name");
        final ArrayList<FileToOpen> fileToOpens = new ArrayList<>();
        final JSONArray filesArray = jsonObject.optJSONArray("filesToOpen");
        if (filesArray != null){
          for (int j = 0; j < filesArray.length(); j++) {
            final JSONObject filesToOpen = filesArray.getJSONObject(j);
            String uri = filesToOpen.getString("uri");
            Level level = Level.parse(filesToOpen.getString("level"));
            OpenMode openMode = OpenMode.valueOf(filesToOpen.getString("openMode"));
            String logImporter = filesToOpen.optString("logImporter", null);
            fileToOpens.add(new FileToOpen(uri, openMode, level, Optional.ofNullable(logImporter)));
          }
        }
        sessions.add(new Session(name, fileToOpens));
      }
    } catch (JSONException e) {
      LOGGER.error("Can't deserialize sessions: ", e);
      Optional.empty();
    }
    LOGGER.info("Returning deserialized sessions: " + sessions.size());
    return Optional.of(sessions);
  }
}
