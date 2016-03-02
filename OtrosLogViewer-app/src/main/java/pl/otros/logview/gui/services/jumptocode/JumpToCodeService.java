package pl.otros.logview.gui.services.jumptocode;

import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.ide.Ide;

import java.io.IOException;
import java.util.Optional;

public interface JumpToCodeService {
  String DEFAULT_HOST = "localhost";
  Integer DEFAULT_PORT = 5987;

  void clearLocationCaches();


  boolean isIdeAvailable();
  boolean isIdeAvailable(String host, int port);
  
  Ide getIde();

  void jump(LocationInfo locationInfo) throws IOException;

  boolean isJumpable(LocationInfo locationInfo) throws IOException;

  String getContent(LocationInfo locationInfo) throws IOException;

  default Optional<String> getContentOptional(LocationInfo locationInfo){
    if (locationInfo == null){
      return Optional.empty();
    }
    try {
      return Optional.of(getContent(locationInfo));
    } catch (Exception e){
      return Optional.empty();
    }
  }

}
