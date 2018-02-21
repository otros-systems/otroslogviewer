package pl.otros.logview.gui.renderers;

import pl.otros.logview.api.model.TimeDelta;
import pl.otros.logview.util.DateUtil;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Date;

public class TimeDeltaRenderer implements TableCellRenderer {



  private Date selectedTimestamp;
  private final JLabel label;

  public TimeDeltaRenderer() {
    label = new JLabel();
    label.setOpaque(true);
    label.setHorizontalAlignment(SwingConstants.RIGHT);

  }

  @Override
  public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
    if (o != null && o instanceof TimeDelta && selectedTimestamp != null) {
      TimeDelta timeDelta = (TimeDelta) o;
      label.setText(DateUtil.formatDelta(timeDelta.getTimestamp().getTime() - selectedTimestamp.getTime()));
    } else {
      label.setText("?");
    }
    return label;
  }


  public void setSelectedTimestamp(Date selectedTimestamp) {
    this.selectedTimestamp = selectedTimestamp;
  }
}
