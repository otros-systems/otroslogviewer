package pl.otros.logview.api.services;

public interface PersistService {
  <T> void persist(String key, T o, Serializer<T, String> serializer) throws Exception;

  <T> T load(String key, T defaultValue, Deserializer<T, String> deserializer);
}
