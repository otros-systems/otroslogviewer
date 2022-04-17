package pl.otros.logview.api.services;

import java.util.Optional;

public interface Deserializer<T, V> {
  Optional<T> deserialize(V data);
}
