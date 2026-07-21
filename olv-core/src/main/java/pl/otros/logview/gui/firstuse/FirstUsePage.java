package pl.otros.logview.gui.firstuse;

import javax.swing.*;

interface FirstUsePage {

  String getTitle();

  String getDescription();

  JComponent getView();

  default void onPageShown(WizardContext context) {
    //do nothing
  }

  default boolean onNext(WizardContext context) {
    return true;
  }
}
