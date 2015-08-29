package pl.otros.logview.gui.services.jumptocode;

import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.ide.Ide;

import java.io.IOException;

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

}
