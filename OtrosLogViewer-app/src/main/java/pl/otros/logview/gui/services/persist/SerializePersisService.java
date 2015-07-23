package pl.otros.logview.gui.services.persist;

import pl.otros.logview.pluginable.AllPluginables;

import java.io.*;
import java.util.Objects;

//TODO temoprary perist ustil, use jackoson instead
public class SerializePersisService implements PersistService {

  private File dir;

  public SerializePersisService() {
    this(new File(AllPluginables.USER_CONFIGURATION_DIRECTORY, "persist"));
  }

  public SerializePersisService(File dir) {
    this.dir = dir;
    if (!dir.exists()){
      dir.mkdirs();
    }
  }

  @Override
  public void persist(String key, Object o) throws Exception {
    final File file = new File(dir, key + ".ser");
    final ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file,false));
    outputStream.writeObject(o);
    outputStream.close();
  }

  @Override
  public <T> T load(String key, T defaultValue) {
    final File file = new File(dir, key + ".ser");
    if (!file.exists()){
      return defaultValue;
    }
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(file));
      final Object o = in.readObject();
      if (o == null || !Objects.equals(o.getClass().getName(), defaultValue.getClass().getName())){
        return defaultValue;
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      //TODO LOGGING
      return defaultValue;
    }
    return defaultValue;
  }
}

