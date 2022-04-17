package pl.otros.logview.api.services;

public interface Serializer<T, V> {
  V serialize(T value);
}
