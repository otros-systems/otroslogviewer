package pl.otros.logview.api.services;

public interface PersistService {
  void persist(String key, Object o) throws Exception;

  <T> T load(String key, T defaultValue);
}
