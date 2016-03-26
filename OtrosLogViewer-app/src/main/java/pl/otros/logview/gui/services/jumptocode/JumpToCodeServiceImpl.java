package pl.otros.logview.gui.services.jumptocode;

import org.apache.commons.configuration.Configuration;
import pl.otros.logview.api.model.LocationInfo;
import pl.otros.logview.api.services.JumpToCodeService;
import pl.otros.logview.api.Ide;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JumpToCodeServiceImpl implements JumpToCodeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JumpToCodeServiceImpl.class.getName());

  public JumpToCodeClient jumpToCodeClient;

  public JumpToCodeServiceImpl(Configuration configuration) {
    jumpToCodeClient = new JumpToCodeClient(configuration);
  }

  @Override
  public void clearLocationCaches() {
    jumpToCodeClient.clearLocationCaches();
  }

  @Override
  public boolean isIdeAvailable() {
    Ide ide = jumpToCodeClient.getIde();
    return ide != null && !Ide.DISCONNECTED.equals(ide);
  }

  @Override
  public boolean isIdeAvailable(String host, int port) {
    Ide ide = jumpToCodeClient.getIde(host, port);
    return ide != null && !Ide.DISCONNECTED.equals(ide);
  }

  @Override
  public Ide getIde() {
    return jumpToCodeClient.getIde();
  }


  @Override
  public void jump(LocationInfo locationInfo) throws IOException {
    String url = jumpToCodeClient.getUrl(locationInfo);
    if (url != null) {
      LOGGER.trace("Jumping to location " + locationInfo + " by opening URL: " + url);
      jumpToCodeClient.jumpTo(url);
    } else {
      LOGGER.trace("Can't jump to " + locationInfo);
    }
  }

  @Override
  public boolean isJumpable(LocationInfo locationInfo) throws IOException {
    boolean jumpable = jumpToCodeClient.isJumpable(locationInfo);
    LOGGER.trace("Checking if location " + locationInfo + " is jumpable: " + jumpable);
    return jumpable;
  }

  @Override
  public String getContent(LocationInfo locationInfo) throws IOException {
    return jumpToCodeClient.getContent(locationInfo);
  }
}
