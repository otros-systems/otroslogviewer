package pl.otros.logview.gui.renderers;

import pl.otros.logview.gui.TimeDelta;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Date;

public class TimeDeltaRenderer extends DefaultTableCellRenderer {

  public static final int SECOND = 1000;
  public static final int MINUTE = 60 * SECOND;
  public static final int HOUR = 60 * MINUTE;

  private Date selectedTimestamp;

  @Override
  public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
    final JLabel label = (JLabel) super.getTableCellRendererComponent(jTable, o, b, b1, i, i1);
    label.setHorizontalAlignment(SwingConstants.RIGHT);

    if (o != null && o instanceof TimeDelta && selectedTimestamp != null) {
      TimeDelta timeDelta = (TimeDelta) o;
      label.setText(formatDelta(timeDelta.getTimestamp().getTime() - selectedTimestamp.getTime()));
    } else {
      label.setText("?");
    }
    return label;
  }

  /**
   * Format time duration to descriptive form like 1,3s, 5h, 2h 3m
   *
   * @param deltaInMillis duration in milliseconds
   * @return time duration in descriptive form
   */
  String formatDelta(long deltaInMillis) {
    StringBuilder sb = new StringBuilder();
    if (deltaInMillis < 0) {
      sb.append("-");
    }
    final long abs = Math.abs(deltaInMillis);
    final long millis = abs % SECOND;
    final long seconds = (abs % MINUTE) / SECOND;
    final long minutes = (abs % HOUR) / MINUTE;
    final long hours = (long) Math.floor(((double) abs) / HOUR);
    if (hours > 3) {
      sb.append(hours).append("h");
    } else if (hours > 0) {
      sb.append(hours).append("h");
      if (minutes > 0) {
        sb.append(" ").append(minutes).append("m");
      }
    } else if (minutes > 4) {
      sb.append(minutes).append("m");
    } else if (minutes > 0) {
      sb.append(minutes).append("m");
      if (seconds > 0) {
        sb.append(" ").append(seconds).append("s");
      }
    } else if (seconds > 3) {
      sb.append(seconds).append("s");
    } else if (seconds > 0) {
      sb.append(seconds);
      if (millis != 0) {
        final String s = Long.toString(Math.round((double) millis / 100));
        sb.append(",").append(s);
      }
      sb.append("s");
    } else {
      sb.append(millis).append("ms");
    }
    return sb.toString();
  }

  public void setSelectedTimestamp(Date selectedTimestamp) {
    this.selectedTimestamp = selectedTimestamp;
  }
}
