package pl.otros.logview.gui.services.persist;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.api.services.Deserializer;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.Serializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class SerializePersisService implements PersistService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SerializePersisService.class.getName());
  private final File dir;

  public SerializePersisService() {
    this(new File(AllPluginables.USER_CONFIGURATION_DIRECTORY, "persist"));
  }

  public SerializePersisService(File dir) {
    this.dir = dir;
    if (!dir.exists()) {
      final boolean mkdirs = dir.mkdirs();
      LOGGER.debug("Dirs {} created: {}", dir, mkdirs);
    }
  }

  @Override
  public <T> void persist(String key, T value, Serializer<T, String> serializer) throws Exception {
    final File file = new File(dir, key + ".value");
    try (Writer writer = new FileWriter(file, false)) {
      writer.write(serializer.serialize(value));
      LOGGER.debug("Successfully persisted value for {} in {}", key, file.getAbsolutePath());
    } catch (IOException e) {
      LOGGER.warn("Can't persist value in file " + file.getAbsolutePath(), e);
      throw e;
    }
  }

  @Override
  public <T> T load(String key, T defaultValue, Deserializer<T, String> deserializer) {
    final File file = new File(dir, key + ".value");
    if (!file.exists()) {
      return defaultValue;
    }
    try (Reader in = (new FileReader(file))) {
      final String string = IOUtils.toString(in);
      return deserializer.deserialize(string).orElse(defaultValue);
    } catch (IOException e) {
      LOGGER.warn("Can't read value for " + key, e);
      return defaultValue;
    }
  }

}

