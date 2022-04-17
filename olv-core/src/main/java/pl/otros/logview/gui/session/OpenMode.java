package pl.otros.logview.gui.session;

import pl.otros.swing.Named;

public enum OpenMode implements Named {
  FROM_START("From start"), FROM_END("From end");
  private String name;

  OpenMode(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
