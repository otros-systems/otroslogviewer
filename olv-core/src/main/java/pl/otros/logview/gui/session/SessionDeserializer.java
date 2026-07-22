package pl.otros.logview.gui.session;

import com.google.gson.*;
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
      final JsonElement jsonElement = JsonParser.parseString(data);
      if (jsonElement.isJsonArray()) {
        final JsonArray array = jsonElement.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
          final JsonObject jsonObject = array.get(i).getAsJsonObject();
          String name = jsonObject.get("name").getAsString();
          final ArrayList<FileToOpen> fileToOpens = new ArrayList<>();
          if (jsonObject.has("filesToOpen") && jsonObject.get("filesToOpen").isJsonArray()) {
            final JsonArray filesArray = jsonObject.getAsJsonArray("filesToOpen");
            for (int j = 0; j < filesArray.size(); j++) {
              final JsonObject filesToOpen = filesArray.get(j).getAsJsonObject();
              String uri = filesToOpen.get("uri").getAsString();
              Level level = Level.parse(filesToOpen.get("level").getAsString());
              OpenMode openMode = OpenMode.valueOf(filesToOpen.get("openMode").getAsString());
              String logImporter = filesToOpen.has("logImporter") ? filesToOpen.get("logImporter").getAsString() : null;
              fileToOpens.add(new FileToOpen(uri, openMode, level, Optional.ofNullable(logImporter)));
            }
          }
          sessions.add(new Session(name, fileToOpens));
        }
      }
    } catch (JsonSyntaxException | IllegalStateException e) {
      LOGGER.error("Can't deserialize sessions: ", e);
      return Optional.empty();
    }
    LOGGER.info("Returning deserialized sessions: " + sessions.size());
    return Optional.of(sessions);
  }
}
