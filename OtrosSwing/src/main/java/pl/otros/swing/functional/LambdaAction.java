package pl.otros.swing.functional;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LambdaAction extends AbstractAction {

  private final ActionListener actionListener;

  public LambdaAction(ActionListener actionListener) {
    this.actionListener = actionListener;
  }
  public LambdaAction(String name, ActionListener actionListener) {
    this(actionListener);
    putValue(NAME,name);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    actionListener.actionPerformed(e);
  }
}
