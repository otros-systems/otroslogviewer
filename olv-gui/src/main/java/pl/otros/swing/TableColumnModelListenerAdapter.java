package pl.otros.swing;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

public class TableColumnModelListenerAdapter implements TableColumnModelListener {
  @Override
  public void columnAdded(TableColumnModelEvent e) {
    //Dummy implementations
  }

  @Override
  public void columnRemoved(TableColumnModelEvent e) {
    //Dummy implementations
  }

  @Override
  public void columnMoved(TableColumnModelEvent e) {
    //Dummy implementations
  }

  @Override
  public void columnMarginChanged(ChangeEvent e) {
    //Dummy implementations
  }

  @Override
  public void columnSelectionChanged(ListSelectionEvent e) {
    //Dummy implementations
  }
}
