package pl.otros.logview.gui.services.jumptocode;

import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.ide.Ide;

import java.io.IOException;

public interface JumpToCodeService {
  public static final String DEFAULT_HOST = "localhost";
  public static final Integer DEFAULT_PORT = 5987;

  void clearLocationCaches();


  public boolean isIdeAvailable();
  public boolean isIdeAvailable(String host, int port);
  
  public Ide getIde();

  public void jump(LocationInfo locationInfo) throws IOException;

  public boolean isJumpable(LocationInfo locationInfo) throws IOException;

  String getContent(LocationInfo locationInfo) throws IOException;

}
