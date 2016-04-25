package pl.otros.logview.gui.services.persist;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.api.services.PersistService;

import java.io.*;
import java.util.Objects;

//TODO temporary persist util, use jackson instead
public class SerializePersisService implements PersistService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SerializePersisService.class.getName());
  private final File dir;

  public SerializePersisService() {
    this(new File(AllPluginables.USER_CONFIGURATION_DIRECTORY, "persist"));
  }

  public SerializePersisService(File dir) {
    this.dir = dir;
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  @Override
  public void persist(String key, Object o) throws Exception {
    final File file = new File(dir, key + ".ser");
    ObjectOutputStream outputStream = null;
    try {
      outputStream = new ObjectOutputStream(new FileOutputStream(file, false));
      outputStream.writeObject(o);
    } catch (IOException e) {
      throw e;
    } finally {
      IOUtils.closeQuietly(outputStream);
    }
  }

  @Override
  public <T> T load(String key, T defaultValue) {
    final File file = new File(dir, key + ".ser");
    if (!file.exists()) {
      return defaultValue;
    }
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(file));
      final Object o = in.readObject();
      if (o == null || !Objects.equals(o.getClass().getName(), defaultValue.getClass().getName())) {
        LOGGER.warn("Read object is null or class different than default value: {}", o);
        return defaultValue;
      }
      return (T) o;
    } catch (IOException | ClassNotFoundException e) {
      LOGGER.warn("Can't read value for " + key, e);
      return defaultValue;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}

