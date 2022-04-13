package pl.otros.logview.accept;

import com.google.common.base.Joiner;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;

import javax.swing.*;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class SelectedThreadAcceptCondition extends SelectionAwareAcceptCondition {

  protected Set<String> threads;

  public SelectedThreadAcceptCondition(JTable jTable, LogDataTableModel dataTableModel) {
    super(jTable, dataTableModel);
  }

  @Override
  protected void init() {
    name = "Selected thread";
    description = name;
    threads = new TreeSet<>();
  }

  @Override
  public boolean accept(LogData data) {
    return threads.contains(data.getThread());
  }

  @Override
  protected void updateAfterSelection() {
    threads.clear();
    int[] selectedRows = jTable.getSelectedRows();
    for (int i : selectedRows) {
      LogData logData = dataTableModel.getLogData(jTable.convertRowIndexToModel(i));
      threads.add(Optional.ofNullable(logData.getThread()).orElse("--"));
    }

    description = threads.size() + " threads: " + Joiner.on(", ").join(threads);
    name = description;
    if (name.length() > NAME_LIMIT) {
      name = name.substring(0, NAME_LIMIT - 3) + "...";
    }
  }
}
