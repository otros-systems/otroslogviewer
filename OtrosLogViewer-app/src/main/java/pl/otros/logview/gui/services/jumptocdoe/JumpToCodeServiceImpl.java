package pl.otros.logview.gui.services.jumptocdoe;

import org.apache.commons.configuration.Configuration;
import pl.otros.logview.gui.message.LocationInfo;

import java.io.IOException;
import java.util.logging.Logger;

public class JumpToCodeServiceImpl implements JumpToCodeService {

  private static final Logger LOGGER = Logger.getLogger(JumpToCodeServiceImpl.class.getName());

  public JumpToCodeClient jumpToCodeClient;
  public JumpToCodeServiceImpl(Configuration configuration){
    jumpToCodeClient = new JumpToCodeClient(configuration);
  }

  @Override
  public void clearLocationCaches() {
    jumpToCodeClient.clearLocationCaches();
  }

  @Override
  public boolean isIdeAvailable() {
    IDE ide = jumpToCodeClient.getIde();
    return ide !=null && !IDE.DISONECTED.equals(ide);
  }

  @Override
  public IDE getIde() {
    return jumpToCodeClient.getIde();
  }


  @Override
  public void jump(LocationInfo locationInfo) throws IOException {
    String url = jumpToCodeClient.getUrl(locationInfo);
    LOGGER.finest("Jumping to location " + locationInfo + " by opening URL: " + url);
    jumpToCodeClient.jumpTo(url);
    
  }

  @Override
  public boolean isJumpable(LocationInfo locationInfo) throws IOException {
    boolean jumpable = jumpToCodeClient.isJumpable(locationInfo);
    LOGGER.finest("Checking if location " + locationInfo + " is jumpable: " + jumpable);
    return jumpable;
  }

  @Override
  public String getContent(LocationInfo locationInfo) throws IOException {
    return jumpToCodeClient.getContent(locationInfo);
  }
}
