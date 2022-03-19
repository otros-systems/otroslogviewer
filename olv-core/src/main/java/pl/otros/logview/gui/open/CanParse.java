package pl.otros.logview.gui.open;

import pl.otros.swing.Named;

enum CanParse implements Named {
  YES("Yes"), NO("No"), FILE_TOO_SMALL("File too small"), NOT_TESTED("Not checked"), TESTING("Checking"), TESTING_ERROR("Testing error");
  private String name;

  CanParse(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
