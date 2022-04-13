package pl.otros.logview.api.services;

import pl.otros.logview.api.Ide;
import pl.otros.logview.api.model.LocationInfo;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public interface JumpToCodeService {

  enum Capabilities {
    JumpByLine("jumpByLine"),
    JumpByMessage("jumpByMessage"),
    ContentByLine("contentByLine"),
    ContentByMessage("contentByMessage"),
    AllFile("allFile"),
    LoggersConfig("loggersConfig");

    private String value;

    Capabilities(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  String DEFAULT_HOST = "localhost";
  Integer DEFAULT_PORT = 5987;

  void clearLocationCaches();


  boolean isIdeAvailable();

  boolean isIdeAvailable(String host, int port);

  Ide getIde();

  void jump(LocationInfo locationInfo) throws IOException;

  boolean isJumpable(LocationInfo locationInfo) throws IOException;

  String getContent(LocationInfo locationInfo) throws IOException;

  Set<Capabilities> capabilities() throws IOException;

  Set<String> loggerPatterns() throws  IOException;

  default Optional<String> getContentOptional(LocationInfo locationInfo) {
    if (locationInfo == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(getContent(locationInfo));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

}
