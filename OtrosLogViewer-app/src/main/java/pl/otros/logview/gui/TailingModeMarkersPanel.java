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
package pl.otros.logview.gui;

import net.miginfocom.swing.MigLayout;
import pl.otros.logview.api.Icons;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.AutomaticMarker;
import pl.otros.logview.api.LogDataTableModel;
import pl.otros.logview.gui.markers.PropertyFileAbstractMarker;
import pl.otros.logview.gui.renderers.AutomaticMarkerRenderer;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.api.pluginable.PluginableElementEventListener;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class TailingModeMarkersPanel extends JPanel implements PluginableElementEventListener<AutomaticMarker>, TableModelListener {

  private final LogDataTableModel dataTableModel;
  private final JComboBox selectedGroup;
  private final DefaultComboBoxModel boxModel;
  private final JTable table;
  private final SelectedMarkersTableModel defaultTableModel;
  private final PluginableElementsContainer<AutomaticMarker> markersContainser;

  public TailingModeMarkersPanel(LogDataTableModel logDataTableModel) {
    super(new MigLayout("wrap 2", "[] [grow]", ""));
    this.dataTableModel = logDataTableModel;

    defaultTableModel = new SelectedMarkersTableModel();
    table = new JTable(defaultTableModel);
    table.getColumnModel().getColumn(0).setMaxWidth(16);

    TableRowSorter<SelectedMarkersTableModel> rowSorter = new TableRowSorter<>(defaultTableModel);
    MarkersRowFilter markersRowFilter = new MarkersRowFilter();
    rowSorter.setRowFilter(markersRowFilter);
    table.setRowSorter(rowSorter);
    table.setOpaque(true);
    // table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
    table.setDefaultRenderer(Object.class, new AutomaticMarkerRenderer());

    boxModel = new DefaultComboBoxModel();
    selectedGroup = new JComboBox(boxModel);
    selectedGroup.addActionListener(e -> defaultTableModel.fireTableDataChanged());

    logDataTableModel.addTableModelListener(this);
    markersContainser = AllPluginables.getInstance().getMarkersContainser();
    markersContainser.addListener(defaultTableModel);
    markersContainser.addListener(this);
    updateGroups(markersContainser.getElements());
    createView();

    defaultTableModel.data.addAll(markersContainser.getElements());
    defaultTableModel.fireTableDataChanged();
  }

  public void createView() {
    JLabel jLabel = new JLabel("Mark incoming events", Icons.MARKER, SwingConstants.LEFT);
    jLabel.setFont(getFont().deriveFont(Font.BOLD));
    this.add(jLabel, "span, wrap");
    this.add(new JLabel("Filter markers group:"));
    this.add(selectedGroup, "wrap");
    this.add(table, "span, growx");
  }

  private void updateGroups(Collection<AutomaticMarker> markers) {
    Object selectedItem = boxModel.getSelectedItem();
    if (boxModel.getIndexOf("--ALL--") < 0) {
      boxModel.addElement("--ALL--");
    }

    TreeSet<String> groups = new TreeSet<>();
    for (AutomaticMarker automaticMarker : markers) {
      for (String g : automaticMarker.getMarkerGroups()) {
        if (g.length() > 0) {
          groups.add(g);
        }
      }
    }
    groups.stream().filter(string -> boxModel.getIndexOf(string) < 0).forEach(boxModel::addElement);
    for (int i = boxModel.getSize() - 1; i > 0; i--) {
      if (!groups.contains(boxModel.getElementAt(i))) {
        boxModel.removeElementAt(i);
      }
    }

    if (boxModel.getIndexOf(selectedItem) > 0) {
      selectedGroup.setSelectedItem(selectedItem);
    }
  }

  @Override
  public void elementAdded(AutomaticMarker marker) {
    updateGroups(markersContainser.getElements());

  }

  @Override
  public void elementRemoved(AutomaticMarker marker) {
    updateGroups(markersContainser.getElements());
  }

  @Override
  public void elementChanged(AutomaticMarker marker) {
    updateGroups(markersContainser.getElements());

  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    final HashSet<AutomaticMarker> selected = defaultTableModel.getSelected();

    if (e.getType() == TableModelEvent.INSERT) {
      SwingUtilities.invokeLater(() -> {
        int firstRow = e.getFirstRow();
        int lastRow = e.getLastRow();
        for (int i = firstRow; i <= lastRow; i++) {
          LogData logData = dataTableModel.getLogData(i);
          for (AutomaticMarker m : selected) {
            if (m.toMark(logData)) {
              dataTableModel.markRows(m.getColors(), i);
            }
          }
        }
      });
    }
  }

  class MarkersRowFilter extends RowFilter<SelectedMarkersTableModel, Integer> {

    @Override
    public boolean include(javax.swing.RowFilter.Entry<? extends SelectedMarkersTableModel, ? extends Integer> arg0) {
      if (selectedGroup.getSelectedIndex() == 0) {
        return true;
      }
      SelectedMarkersTableModel model = arg0.getModel();
      Integer identifier = arg0.getIdentifier();
      AutomaticMarker valueAt = model.data.get(identifier.intValue());
      String[] markerGroups = valueAt.getMarkerGroups();
      return Arrays.asList(markerGroups).contains(selectedGroup.getSelectedItem());
    }
  }

  class SelectedMarkersTableModel extends AbstractTableModel implements PluginableElementEventListener<AutomaticMarker> {

    private final List<AutomaticMarker> data = new ArrayList<>();
    private final HashSet<AutomaticMarker> selected = new HashSet<>();

    public HashSet<AutomaticMarker> getSelected() {
      synchronized (data) {
        return new HashSet<>(selected);
      }
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public int getRowCount() {
      synchronized (data) {
        return data.size();
      }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex == 0 && aValue instanceof Boolean) {
        Boolean isSelected = (Boolean) aValue;
        if (isSelected) {
          selected.add(data.get(rowIndex));
        } else {
          selected.remove(data.get(rowIndex));
        }
      }
    }

    @Override
    public Object getValueAt(int row, int col) {
      if (col == 0) {
        return selected.contains(data.get(row));
      } else {
        return data.get(row);
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int col) {
      return col == 0 ? Boolean.class : Object.class;
    }

    @Override
    public void elementAdded(AutomaticMarker marker) {
      synchronized (data) {
        data.add(marker);
      }
      fireTableDataChanged();
    }

    @Override
    public void elementRemoved(AutomaticMarker marker) {
      synchronized (data) {
        data.remove(marker);
        selected.remove(marker);
      }
      fireTableDataChanged();

    }

    @Override
    public void elementChanged(AutomaticMarker marker) {
      synchronized (data) {
        // TODO refactor!
        if (marker instanceof PropertyFileAbstractMarker) {
          PropertyFileAbstractMarker changedMarker = (PropertyFileAbstractMarker) marker;
          for (AutomaticMarker m : defaultTableModel.data) {
            if (m instanceof PropertyFileAbstractMarker) {
              PropertyFileAbstractMarker oldMarker = (PropertyFileAbstractMarker) m;
              if (oldMarker.getFileName().equals(changedMarker.getFileName())) {
                if (defaultTableModel.selected.contains(changedMarker)) {
                  defaultTableModel.selected.add(oldMarker);
                }
                defaultTableModel.data.remove(oldMarker);
                defaultTableModel.data.add(changedMarker);
                break;
              }
            }
          }
        }
      }
      fireTableDataChanged();
    }

  }
}
