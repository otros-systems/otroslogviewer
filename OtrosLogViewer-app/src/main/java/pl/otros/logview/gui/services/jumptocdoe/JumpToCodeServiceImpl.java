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
  public boolean isIdeAvailable() {
    return jumpToCodeClient.getIde()!=null;
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
    return jumpToCodeClient.isJumpable(locationInfo);
  }
}
