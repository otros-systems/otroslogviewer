/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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
 ******************************************************************************/
package pl.otros.logview.filter;

import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class TimeFilter extends AbstractLogFilter {

  private static final String NAME = "Time filter";
  private static final String DESCRIPTION = "Filtering events based on event time.";

  private Date start, end;
  private final SpinnerDateModel startM;
  private final SpinnerDateModel endM;
  private final JPanel gui;
  private final ChangeListner changeListner;
  private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final JCheckBox startEnable;
  private final JCheckBox endEnable;

  public TimeFilter() {
    super(NAME, DESCRIPTION);
    changeListner = new ChangeListner();
    start = new Date();
    end = new Date();
    startM = new SpinnerDateModel();
    startM.setValue(start);
    startM.addChangeListener(changeListner);
    endM = new SpinnerDateModel();
    endM.setValue(end);
    endM.addChangeListener(changeListner);
    startEnable = new JCheckBox("Show after date:");
    startEnable.addActionListener(e -> listener.valueChanged());
    endEnable = new JCheckBox("Show before date:");
    endEnable.addActionListener(e -> listener.valueChanged());
    gui = new JPanel(new GridLayout(4, 1));
    JSpinner startSpinner = new JSpinner(startM);
    JSpinner endSpinner = new JSpinner(endM);
    gui.add(startEnable);
    gui.add(startSpinner);
    gui.add(endEnable);
    gui.add(endSpinner);
  }

  @Override
  public boolean accept(LogData logData, int row) {
    Date d = logData.getDate();
    boolean accept = true;
    if (startEnable.isSelected()) {
      accept = d.getTime() >= start.getTime();
    }
    if (endEnable.isSelected()) {
      accept = accept && d.getTime() <= end.getTime();
    }

    return accept;
  }

  @Override
  public Component getGUI() {

    return gui;
  }

  @Override
  public void init(Properties properties, LogDataTableModel collector) {

  }

  private class ChangeListner implements ChangeListener {

    @Override
    public void stateChanged(ChangeEvent e) {
      start = startM.getDate();
      end = endM.getDate();
      listener.valueChanged();

    }

  }

  public void setStart(Date date) {
    startM.setValue(date);
  }

  public void setEnd(Date date) {
    endM.setValue(date);
  }

  public void setStartFilteringEnabled(boolean b) {
    startEnable.setSelected(b);
    listener.valueChanged();
  }

  public void setFilteringEndEnabled(boolean b) {
    endEnable.setSelected(b);
    listener.valueChanged();
  }
}
