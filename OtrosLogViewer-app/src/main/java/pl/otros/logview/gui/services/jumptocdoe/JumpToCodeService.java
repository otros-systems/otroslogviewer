package pl.otros.logview.gui.services.jumptocdoe;

import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.message.LocationInfo;

import javax.swing.*;
import java.io.IOException;

public interface JumpToCodeService {
  
  public enum IDE {
    Eclipse(Icons.IDE_ECLIPSE,Icons.IDE_ECLIPSE_DISCONNCTED),
    IDEA(Icons.IDE_IDEA,Icons.IDE_IDEA_DISCONNCTED);
    Icon iconConnected, iconDiscounted;

    IDE(Icon iconConnected, Icon iconDiscounted) {
      this.iconConnected = iconConnected;
      this.iconDiscounted = iconDiscounted;
    }

    public Icon getIconDiscounted() {
      return iconDiscounted;
    }

    public Icon getIconConnected() {
      return iconConnected;
    }
  }
  
  public boolean isIdeAvailable();
  
  public IDE getIde();

  public void jump(LocationInfo locationInfo) throws IOException;

  public boolean isJumpable(LocationInfo locationInfo) throws IOException;

}
