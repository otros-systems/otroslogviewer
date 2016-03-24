package pl.otros.logview.api;

import javax.swing.*;

/**
*/
public enum Ide {
  Eclipse(Icons.IDE_ECLIPSE,Icons.ICE_ECLIPSE_DISCONNECTED),
  IDEA(Icons.IDE_IDEA,Icons.IDE_IDEA_DISCONNCTED),
  DISCONNECTED(null,null);
  Icon iconConnected, iconDiscounted;

  Ide(Icon iconConnected, Icon iconDiscounted) {
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
