package pl.otros.swing.functional;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentChangeListener implements DocumentListener {

  private Runnable update;

  public DocumentChangeListener(Runnable update) {
    this.update = update;
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    update.run();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    update.run();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    update.run();
  }
}
