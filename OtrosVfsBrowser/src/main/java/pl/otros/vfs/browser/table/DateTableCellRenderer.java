/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.vfs.browser.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTableCellRenderer extends DefaultTableCellRenderer {

  private DateFormat dateFormatFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private DateFormat dateFormatHourOnly = new SimpleDateFormat("HH:mm:ss");
  private DateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    Date d = (Date) value;
    String s = "-";
    if (d != null) {
      s = d.toString();
      DateFormat df = dateOnly;
      if (dateOnly.format(d).equals(dateOnly.format(new Date()))) {
        df = dateFormatHourOnly;
        l.setToolTipText(dateFormatFull.format(d));
      }
      l.setToolTipText(dateFormatFull.format(d));
      s = df.format(d);

    }
    l.setText(s);
    l.setHorizontalAlignment(SwingConstants.RIGHT);

    return l;

  }

}
