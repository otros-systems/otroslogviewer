/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.gui;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.table.ColumnControlButton;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataCollector;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.accept.AcceptCondition;
import pl.otros.logview.accept.FilteredAcceptCondition;
import pl.otros.logview.accept.HigherIdAcceptCondition;
import pl.otros.logview.accept.LevelLowerAcceptCondition;
import pl.otros.logview.accept.LowerIdAcceptCondition;
import pl.otros.logview.accept.PropertyAcceptCondition;
import pl.otros.logview.accept.SelectedClassAcceptCondition;
import pl.otros.logview.accept.SelectedEventsAcceptCondition;
import pl.otros.logview.accept.SelectedThreadAcceptCondition;
import pl.otros.logview.api.plugins.MenuActionProvider;
import pl.otros.logview.filter.CallHierarchyLogFilter;
import pl.otros.logview.filter.ClassFilter;
import pl.otros.logview.filter.FilterPanel;
import pl.otros.logview.filter.LogFilter;
import pl.otros.logview.filter.LogFilterValueChangeListener;
import pl.otros.logview.filter.LoggerNameFilter;
import pl.otros.logview.filter.PropertyFilter;
import pl.otros.logview.filter.ThreadFilter;
import pl.otros.logview.filter.TimeFilter;
import pl.otros.logview.gui.actions.AutomaticMarkUnamrkActionListener;
import pl.otros.logview.gui.actions.ClearMarkingsAction;
import pl.otros.logview.gui.actions.CopySelectedText;
import pl.otros.logview.gui.actions.CopyStyledMessageDetailAction;
import pl.otros.logview.gui.actions.FocusOnEventsAfter;
import pl.otros.logview.gui.actions.FocusOnEventsBefore;
import pl.otros.logview.gui.actions.FocusOnSelectedClassesAction;
import pl.otros.logview.gui.actions.FocusOnSelectedLoggerNameAction;
import pl.otros.logview.gui.actions.FocusOnSelectedPropertyAction;
import pl.otros.logview.gui.actions.FocusOnThisThreadAction;
import pl.otros.logview.gui.actions.IgnoreSelectedEventsClasses;
import pl.otros.logview.gui.actions.MarkRowAction;
import pl.otros.logview.gui.actions.OtrosAction;
import pl.otros.logview.gui.actions.RemoveByAcceptanceCriteria;
import pl.otros.logview.gui.actions.ShowCallHierarchyAction;
import pl.otros.logview.gui.actions.TableResizeActionListener;
import pl.otros.logview.gui.actions.UnMarkRowAction;
import pl.otros.logview.gui.actions.table.MarkRowBySpaceKeyListener;
import pl.otros.logview.gui.config.LogTableFormatConfigView;
import pl.otros.logview.gui.markers.AutomaticMarker;
import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.update.MessageDetailListener;
import pl.otros.logview.gui.note.NoteEvent;
import pl.otros.logview.gui.note.NoteEvent.EventType;
import pl.otros.logview.gui.note.NoteObserver;
import pl.otros.logview.gui.renderers.DateRenderer;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.gui.renderers.MarkTableEditor;
import pl.otros.logview.gui.renderers.MarkTableRenderer;
import pl.otros.logview.gui.renderers.NoteRenderer;
import pl.otros.logview.gui.renderers.NoteTableEditor;
import pl.otros.logview.gui.renderers.Renderers;
import pl.otros.logview.gui.renderers.TableMarkDecoratorRenderer;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.gui.table.ColumnLayout;
import pl.otros.logview.gui.table.JTableWith2RowHighliting;
import pl.otros.logview.gui.table.TableColumns;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.logview.pluginable.PluginableElement;
import pl.otros.logview.pluginable.PluginableElementEventListener;
import pl.otros.logview.pluginable.PluginableElementNameComparator;
import pl.otros.logview.pluginable.PluginableElementsContainer;
import pl.otros.logview.pluginable.SynchronizePluginableContainerListener;
import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;
import pl.otros.swing.rulerbar.RulerBarHelper;
import pl.otros.vfs.browser.table.FileSize;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogViewPanel extends JPanel implements LogDataCollector {

  private static final Logger LOGGER = Logger.getLogger(LogViewPanel.class.getName());
  private final OtrosJTextWithRulerScrollPane<JTextPane> logDetailWithRulerScrollPane;
  private final MessageDetailListener messageDetailListener;
  private Font menuLabelFont;
  private JPanel filtersPanel;
  private JPanel logsTablePanel;
  private JPanel logsMarkersPanel;
  private JPanel leftPanel;
  private JMenu automaticMarkersMenu;
  private JMenu automaticUnmarkersMenu;
  private LogDataTableModel dataTableModel;
  private OtrosApplication otrosApplication;
  private JTextPane logDetailTextArea;
  private JXTable table;
  private TableRowSorter<LogDataTableModel> sorter;
  private StatusObserver statusObserver;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  private JTabbedPane jTabbedPane;
  private JTextArea notes;
  //  private JScrollPane scrollPane;
  private FocusOnThisThreadAction focusOnThisThreadAction;
  private FocusOnEventsAfter focusOnEventsAfter;
  private FocusOnEventsBefore focusOnEventsBefore;
  private FocusOnSelectedClassesAction focusOnSelectedClassesAction;
  private FocusOnSelectedLoggerNameAction focusOnSelectedLoggerNameAction;
  private IgnoreSelectedEventsClasses ignoreSelectedEventsClasses;
  private ShowCallHierarchyAction showCallHierarchyAction;
  private PluginableElementsContainer<AutomaticMarker> markersContainer;
  private PluginableElementsContainer<LogFilter> logFiltersContainer;
  private PluginableElementsContainer<MessageColorizer> messageColorizersContainer;
  private PluginableElementsContainer<MessageFormatter> messageFormattersContainer;
  private PluginableElementsContainer<MessageColorizer> selectedMessageColorizersContainer;
  private PluginableElementsContainer<MessageFormatter> selectedMessageFormattersContainer;
  private JToolBar messageDetailToolbar;
  private List<AcceptCondition> acceptConditionList;
  private PropertyFilter propertyFilter;
  private FilterPanel propertyFilterPanel;
  private Collection<LogFilter> filtersList;
  private DataConfiguration configuration;
  private LogData displayedLogData;

  public LogViewPanel(final LogDataTableModel dataTableModel, TableColumns[] visibleColumns, final OtrosApplication otrosApplication) {
    super();
    this.dataTableModel = dataTableModel;
    this.otrosApplication = otrosApplication;
    this.statusObserver = otrosApplication.getStatusObserver();
    configuration = otrosApplication.getConfiguration();

    AllPluginables allPluginable = AllPluginables.getInstance();
    markersContainer = allPluginable.getMarkersContainser();
    markersContainer.addListener(new MarkersMenuReloader());
    logFiltersContainer = allPluginable.getLogFiltersContainer();
    messageColorizersContainer = allPluginable.getMessageColorizers();
    messageFormattersContainer = allPluginable.getMessageFormatters();
    selectedMessageColorizersContainer = new PluginableElementsContainer<MessageColorizer>();
    selectedMessageFormattersContainer = new PluginableElementsContainer<MessageFormatter>();
    for (MessageColorizer messageColorizer : messageColorizersContainer.getElements()) {
      selectedMessageColorizersContainer.addElement(messageColorizer);
    }
    for (MessageFormatter messageFormatter : messageFormattersContainer.getElements()) {
      selectedMessageFormattersContainer.addElement(messageFormatter);
    }
    messageColorizersContainer.addListener(new SynchronizePluginableContainerListener<MessageColorizer>(selectedMessageColorizersContainer));
    messageFormattersContainer.addListener(new SynchronizePluginableContainerListener<MessageFormatter>(selectedMessageFormattersContainer));


    menuLabelFont = new JLabel().getFont().deriveFont(Font.BOLD);
    filtersPanel = new JPanel();
    logsTablePanel = new JPanel();
    logsMarkersPanel = new JPanel();
    leftPanel = new JPanel(new MigLayout());
    logDetailTextArea = new JTextPane();
    logDetailTextArea.setEditable(false);
    MouseAdapter locationInfo = new LocationClickMouseAdapter(otrosApplication, logDetailTextArea);
    logDetailTextArea.addMouseMotionListener(locationInfo);
    logDetailTextArea.addMouseListener(locationInfo);
    logDetailTextArea.setBorder(BorderFactory.createTitledBorder("Details"));
    logDetailWithRulerScrollPane = RulerBarHelper.wrapTextComponent(logDetailTextArea);
    table = new JTableWith2RowHighliting(dataTableModel);

    // Initialize default column visible before creating context menu
    table.setColumnControlVisible(true);
    final ColumnControlButton columnControlButton =
      new ColumnControlButton(table){
        @Override
        protected List<Action> getAdditionalActions() {
            final List<Action> additionalActions = super.getAdditionalActions();
            additionalActions.add(
              new AbstractAction("Save current to new column layout") {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // Consider image disk--plus.png
                    String newLayoutName =
                      JOptionPane.showInputDialog(table, "New Layout name");
                    if (newLayoutName == null) return;
                    newLayoutName = newLayoutName.trim();
                    LOGGER.warning( String.format(
                      "Saving New column layout '%s'", newLayoutName));
                    LOGGER.warning(
                        "TODO:  Validate layout name uses only word charaters");
                    List<String> visibleColNames = new ArrayList();
                    for (TableColumn tc : table.getColumns()) {
                        Object o = tc.getIdentifier();
                        if (!(o instanceof TableColumns)) {
                            LOGGER.severe(
                              "TableColumn idenfier of unexpected type: "
                              + tc.getIdentifier().getClass().getName());
                            LOGGER.warning("Throw up a pop-up");
                            return;
                        }
                        TableColumns tcs = (TableColumns) o;
                        visibleColNames.add(tcs.getName());
                    }
                  ColumnLayout columnLayout = new ColumnLayout(newLayoutName,visibleColNames);
                  final List<ColumnLayout> columnLayouts = LogTableFormatConfigView.loadColumnLayouts(configuration);
                  columnLayouts.add(columnLayout);
                  LogTableFormatConfigView.saveColumnLayouts(columnLayouts,configuration);
                  populatePopup();
                }
            });
            //TODO get column layout from configuration, and create action for every layout
            final List<ColumnLayout> columnLayoutNames = LogTableFormatConfigView.loadColumnLayouts(configuration);
                configuration.getList("colLayout");
            for (final ColumnLayout columnLayout : columnLayoutNames) {
                final String name =  columnLayout.getName();
                additionalActions.add(new AbstractAction(name) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        String retVal =
                          (String) JOptionPane.showInputDialog(table,
                        String.format("Column layout '%s'", name),
                          "Select Column Layout Action",
                          JOptionPane.QUESTION_MESSAGE, null, new String[] {
                              "Apply to view", String.format(
                                "Update '%s' according to current view",
                                name),
                            "Rename", "Remove", "Export"
                          }, "Apply to view");
                        if (retVal == null) {
                            LOGGER.warning("User cancelled col-layout pupup");
                            return;
                        }
                        LOGGER.warning(String.format("Do '%s' to layout '%s'",
                          retVal, name));
                        if (retVal.equals("Apply to view")) {
                            LOGGER.warning("TODO:  Implement Col-Layout Apply action");
                            List<String> colNames = columnLayout.getColumns();
                            LOGGER.warning(String.format(
                              "Retrieved %d col names: <<%s>>",
                              colNames.size(), colNames.toString()));
                            List<TableColumns> visCols =
                              new ArrayList<TableColumns>();
                            Map<String, TableColumns> colNameToEnum =
                              new HashMap<String, TableColumns>();
                            for (TableColumns tcEnum : TableColumns.values())
                                colNameToEnum.put(tcEnum.getName(), tcEnum);

                            for (TableColumn tableColumn : table.getColumns()) {
                                Object o = tableColumn.getIdentifier();
                                if (!(o instanceof TableColumns)) {
                                    LOGGER.severe(
                                      "TableColumn idenfier of unexpected type: "
                                      + tableColumn.getIdentifier().getClass().getName());
                                    LOGGER.warning("Throw up a pop-up");
                                    return;
                                }
                                TableColumns tcs = (TableColumns) o;
                                table.getColumnExt(tcs).setVisible(false);
                            }
                            for (Object colName : colNames)
                                visCols.add(
                                  colNameToEnum.get((String) colName));
                            // Want to JXTable.setColumnSequence but need
                            // "logical column names".  I see nothing about
                            // "logical column names" elsewhere in the APIs.
                            /*
                            //EnumSet<TableColumns> visCols =
                              //EnumSet.noneOf(TableColumns.class);
                            List<TableColumns> visCols =
                              new ArrayList<TableColumns>();
                            for (TableColumns tableColumn : TableColumns.values()) {
                                int modelI = tableColumn.getColumn();
                                // Just to emulate column changes:
                                if (((modelI / 2) * 2) == modelI) // Even
                                    visCols.add(tableColumn);
                            }
                            */
                            showOnlyThisColumns(visCols.toArray(new TableColumns[0]));
                            LOGGER.warning("Column changes applied");
                        } else if (retVal.startsWith("Update")) {
                            LOGGER.warning("TODO:  Implement Col-Layout Update action");
                        } else if (retVal.equals("Rename")) {
                            LOGGER.warning("TODO:  Implement Col-Layout Rename action");
                        } else if (retVal.equals("Remove")) {
                            List list = configuration.getList("colLayout");
                            int origSize = list.size();
                            if (!list.remove(name) ||
                              list.size() != origSize - 1) {
                                LOGGER.severe(String.format(
                                  "Failed to remove '%s' from list.  "
                                  + "Size went from %d to %d", origSize,
                                  list.size()));
                                LOGGER.warning("Throw up a pop-up");
                            }
                            configuration.setProperty("colLayout", list);
                            if (configuration.getString("colLayout." + name)
                              != null)
                                configuration.clearProperty(
                                  "colLayout." + name);
                            populatePopup();
                            LOGGER.warning(String.format(
                              "Removed layout '%s'.  There are now %d.",
                              name, list.size()));
                        } else if (retVal.equals("Export")) {
                            LOGGER.warning("TODO:  Implement Col-Layout Export action");
                        } else {
                            LOGGER.severe(
                              "Unexpected return value from col layout popup: "
                              + retVal);
                            LOGGER.warning("Throw up a pop-up");
                        }
                    }
                });
            }
            return additionalActions;
        }
    };
    table.setColumnControl(columnControlButton);
;

    List<TableColumn> columns = table.getColumns(true);
    for (int i = 0; i < columns.size(); i++) {
      columns.get(i).setIdentifier(TableColumns.getColumnById(i));
    }
    for (TableColumn tableColumn : columns) {
      table.getColumnExt(tableColumn.getIdentifier()).setVisible(false);
    }
    for (TableColumns tableColumns : visibleColumns) {
      table.getColumnExt(tableColumns).setVisible(true);
    }

//    sortColumnsInAlphabeticOrder();

    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    updateColumnsSize();
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    final Renderers renderers = Renderers.getInstance(otrosApplication);
    table.setDefaultRenderer(String.class, new TableMarkDecoratorRenderer(renderers.getStringRenderer()));
    table.setDefaultRenderer(Object.class, new TableMarkDecoratorRenderer(table.getDefaultRenderer(Object.class)));
    table.setDefaultRenderer(Integer.class, new TableMarkDecoratorRenderer(table.getDefaultRenderer(Object.class)));
    table.setDefaultRenderer(Level.class, new TableMarkDecoratorRenderer(renderers.getLevelRenderer()));
    table.setDefaultRenderer(Date.class, new TableMarkDecoratorRenderer(renderers.getDateRenderer()));

    ((EventSource) configuration.getConfiguration()).addConfigurationListener(new ConfigurationListener() {
      @Override
      public void configurationChanged(ConfigurationEvent ce) {
        if (ce.getType() == AbstractConfiguration.EVENT_SET_PROPERTY && !ce.isBeforeUpdate()) {
          if (ce.getPropertyName().equals(ConfKeys.LOG_TABLE_FORMAT_DATE_FORMAT)) {
            table.setDefaultRenderer(Date.class, new TableMarkDecoratorRenderer(new DateRenderer(configuration.getString(ConfKeys.LOG_TABLE_FORMAT_DATE_FORMAT, "HH:mm:ss.SSS"))));
            updateTimeColumnSize();
          } else if (ce.getPropertyName().equals(ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER)) {
            table.setDefaultRenderer(Level.class, new TableMarkDecoratorRenderer(new LevelRenderer(configuration.get(LevelRenderer.Mode.class, ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly))));
            updateLevelColumnSize();
          }
        }
      }
    });

    table.setDefaultRenderer(Boolean.class, new TableMarkDecoratorRenderer(table.getDefaultRenderer(Boolean.class)));
    table.setDefaultRenderer(Note.class, new TableMarkDecoratorRenderer(new NoteRenderer()));
    table.setDefaultRenderer(MarkerColors.class, new TableMarkDecoratorRenderer(new MarkTableRenderer()));
    table.setDefaultEditor(Note.class, new NoteTableEditor());
    table.setDefaultEditor(MarkerColors.class, new MarkTableEditor(otrosApplication));
    table.setDefaultRenderer(ClassWrapper.class, new TableMarkDecoratorRenderer(renderers.getClassWrapperRenderer()));
    sorter = new TableRowSorter<LogDataTableModel>(dataTableModel);
    for (int i = 0; i < dataTableModel.getColumnCount(); i++) {
      sorter.setSortable(i, false);
    }
    sorter.setSortable(TableColumns.ID.getColumn(), true);
    sorter.setSortable(TableColumns.TIME.getColumn(), true);
    table.setRowSorter(sorter);

    messageDetailListener = new MessageDetailListener(this, dateFormat,
        selectedMessageFormattersContainer, selectedMessageColorizersContainer);
    table.getSelectionModel().addListSelectionListener(messageDetailListener);
    dataTableModel.addNoteObserver(messageDetailListener);

    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        boolean hasFocus = otrosApplication.getApplicationJFrame().isFocused();
        final boolean enabled = otrosApplication.getConfiguration().getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, false);
        if (hasFocus && enabled && !e.getValueIsAdjusting()) {
          try {
            final LogData logData = dataTableModel.getLogData(table.convertRowIndexToModel(e.getFirstIndex()));
            LocationInfo li = new LocationInfo(logData.getClazz(), logData.getMethod(), logData.getFile(), Integer.valueOf(logData.getLine()));
            final JumpToCodeService jumpToCodeService = otrosApplication.getServices().getJumpToCodeService();
            final boolean ideAvailable = jumpToCodeService.isIdeAvailable();
            if (ideAvailable) {
              LOGGER.fine("Jumping to " + li);
              jumpToCodeService.jump(li);
            }
          } catch (Exception e1) {
            LOGGER.warning("Can't perform jump to code " + e1.getMessage());
          }

        }
      }
    });


    notes = new JTextArea();
    notes.setEditable(false);
    NoteObserver allNotesObserver = new AllNotesTextAreaObserver(notes);
    dataTableModel.addNoteObserver(allNotesObserver);

    addFiltersGUIsToPanel(filtersPanel);
    logsTablePanel.setLayout(new BorderLayout());
    logsTablePanel.add(new JScrollPane(table));
//    ScrollableHeightOnlyPanel scrollableHeightOnlyPanel = new ScrollableHeightOnlyPanel(new BorderLayout());
//    scrollableHeightOnlyPanel.add(logDetailWithRulerScrollPane);
//    scrollPane = new JScrollPane(logDetailWithRulerScrollPane);
//    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    JPanel messageDetailsPanel = new JPanel(new BorderLayout());
    messageDetailToolbar = new JToolBar("MessageDetail");
    messageDetailsPanel.add(messageDetailToolbar, BorderLayout.NORTH);
    messageDetailsPanel.add(logDetailWithRulerScrollPane);
    initMessageDetailsToolbar();

    jTabbedPane = new JTabbedPane();
    jTabbedPane.add("Message detail", messageDetailsPanel);
    jTabbedPane.add("All notes", new JScrollPane(notes));

    leftPanel.add(filtersPanel, "wrap, growx");
    leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "wrap,growx");
    leftPanel.add(logsMarkersPanel, "wrap,growx");

    JSplitPane splitPaneLogsTableAndDetails = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logsTablePanel, jTabbedPane);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(leftPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), splitPaneLogsTableAndDetails);
    splitPane.setOneTouchExpandable(true);
    this.setLayout(new BorderLayout());
    this.add(splitPane);

    splitPaneLogsTableAndDetails.setDividerLocation(0.5d);
    splitPaneLogsTableAndDetails.setOneTouchExpandable(true);
    splitPane.setDividerLocation(leftPanel.getPreferredSize().width + 10);

    PopupListener popupListener = new PopupListener(new Callable<JPopupMenu>() {
      @Override
      public JPopupMenu call() throws Exception {
        return initTableContextMenu();
      }
    });
    table.addMouseListener(popupListener);
    table.addKeyListener(popupListener);

    PopupListener popupListenerMessageDetailMenu = new PopupListener(new Callable<JPopupMenu>() {
      @Override
      public JPopupMenu call() throws Exception {
        return initMessageDetailPopupMenu();
      }
    });
    logDetailTextArea.addMouseListener(popupListenerMessageDetailMenu);
    logDetailTextArea.addKeyListener(popupListenerMessageDetailMenu);

    dataTableModel.notifyAllNoteObservers(new NoteEvent(EventType.CLEAR, dataTableModel, null, 0));

    table.addKeyListener(new MarkRowBySpaceKeyListener(otrosApplication));
    initAcceptConditions();
  }


  /**
   * Sort column in alphabetic order - column sequencing proof of concept
   */
  protected void sortColumnsInAlphabeticOrder() {
    final int columnCount = table.getColumnCount(false);
    final Map<String,Integer> currentColumnLayout = new HashMap<String, Integer>();
    for (int i=0; i<columnCount; i++){
      final String columnName = table.getColumnName(i);
      currentColumnLayout.put(columnName, i);
    }
    final ArrayList<String> sortedColumnsName = new ArrayList<String>(currentColumnLayout.keySet());
    Collections.sort(sortedColumnsName);
    for (int i=0;i<sortedColumnsName.size();i++){
      final int index = findColumnIndexByHeader(table.getColumnModel(),sortedColumnsName.get(i));
      if(index>-1){
        LOGGER.info("Moving " + index + " to " + i);
        table.moveColumn(index,i);
      }
    }
  }

  /**
   * Find column index by it's name
   * @param columnModel column model
   * @param columnName column name to find
   * @return column index or -1 if not found
   */
  protected Integer findColumnIndexByHeader(TableColumnModel columnModel, String columnName) {
    for (int i=0; i<columnModel.getColumnCount();i++){
      final String header = columnModel.getColumn(i).getHeaderValue().toString();
      if (  StringUtils.equals(header,columnName) ){
        return i;
      }
    }
    LOGGER.warning("Can't find column index for " + columnName);
    return -1;
  }

  private JPopupMenu initMessageDetailPopupMenu() {
    JPopupMenu jPopupMenu = new JPopupMenu();
    jPopupMenu.add(new CopySelectedText(otrosApplication, logDetailTextArea));
    jPopupMenu.add(new CopyStyledMessageDetailAction(otrosApplication, dateFormat, selectedMessageColorizersContainer, selectedMessageFormattersContainer));
    return jPopupMenu;
  }

  private void initAcceptConditions() {
    acceptConditionList = new ArrayList<AcceptCondition>();
    acceptConditionList.add(new SelectedEventsAcceptCondition(table, dataTableModel));
    acceptConditionList.add(new LowerIdAcceptCondition(table, dataTableModel));
    acceptConditionList.add(new HigherIdAcceptCondition(table, dataTableModel));
    acceptConditionList.add(new SelectedClassAcceptCondition(table, dataTableModel));
    acceptConditionList.add(new SelectedThreadAcceptCondition(table, dataTableModel));
    acceptConditionList.add(new LevelLowerAcceptCondition(Level.INFO));
    acceptConditionList.add(new LevelLowerAcceptCondition(Level.WARNING));
    acceptConditionList.add(new LevelLowerAcceptCondition(Level.SEVERE));
    acceptConditionList.add(new FilteredAcceptCondition(filtersList));

  }

  private void updateColumnsSize() {
    FontMetrics fm = table.getFontMetrics(table.getFont());
    updateColumnSizeIfVisible(TableColumns.ID, fm.stringWidth("0000000"), fm.stringWidth("000000000"));
    updateTimeColumnSize();
    updateLevelColumnSize();
    updateColumnSizeIfVisible(TableColumns.CLASS, 100, 400);
    updateColumnSizeIfVisible(TableColumns.THREAD, 100, 400);
    updateColumnSizeIfVisible(TableColumns.METHOD, 100, 200);
    updateColumnSizeIfVisible(TableColumns.LINE, fm.stringWidth("0000"), fm.stringWidth("000000"));
    updateColumnSizeIfVisible(TableColumns.MARK, 16, 16);
    updateColumnSizeIfVisible(TableColumns.NOTE, 100, 1500);
  }

  private void updateTimeColumnSize() {
    FontMetrics fm = table.getFontMetrics(table.getFont());
    int dateWidth = fm.stringWidth(new SimpleDateFormat(configuration.getString(ConfKeys.LOG_TABLE_FORMAT_DATE_FORMAT, "HH:mm:ss.SSS")).format(new Date()));
    updateColumnSizeIfVisible(TableColumns.TIME, dateWidth + 1, dateWidth + 1);
  }

  private void updateLevelColumnSize() {
    FontMetrics fm = table.getFontMetrics(table.getFont());
    int levelWidth = fm.stringWidth(Level.WARNING.getName());

    switch (configuration.get(LevelRenderer.Mode.class, ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly)) {
      case IconsOnly:
        updateColumnSizeIfVisible(TableColumns.LEVEL, 16 + 1, 16 + 1);
        break;
      case IconsAndText:
        updateColumnSizeIfVisible(TableColumns.LEVEL, 16 + levelWidth + 5, 16 + levelWidth + 5);
        break;
      case TextOnly:
        updateColumnSizeIfVisible(TableColumns.LEVEL, levelWidth + 1, levelWidth + 1);
        break;
    }
  }

  private void updateColumnSizeIfVisible(TableColumns column, int width, int maxWidth) {
    table.getColumns(true).get(column.getColumn()).setMaxWidth(maxWidth);
    table.getColumns(true).get(column.getColumn()).setPreferredWidth(width);
  }

  public JTextPane getLogDetailTextArea() {
    return logDetailTextArea;
  }

  public void add(LogData[] autoResizeSubsequent) {
    dataTableModel.add(autoResizeSubsequent);

  }

  public void add(LogData logData) {
    dataTableModel.add(logData);
  }

  public LogData[] getLogData() {
    return dataTableModel.getLogData();
  }

  private void addFiltersGUIsToPanel(JPanel filtersPanel) {
    filtersPanel.setLayout(new MigLayout("", "[grow]", ""));

    Collection<LogFilter> loadedFilters = logFiltersContainer.getElements();

    // Reload filters, every instance of filter is connected to listeners, data table etc.
    filtersList = new ArrayList<LogFilter>();
    for (LogFilter logFilter : loadedFilters) {
      try {
        LogFilter filter = logFilter.getClass().newInstance();
        filtersList.add(filter);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Can't initialize filter: " + logFilter.getClass(), e);
      }

    }
    JLabel filtersLabel = new JLabel("Filters:");
    filtersLabel.setMinimumSize(new Dimension(200, 16));
    filtersLabel.setPreferredSize(new Dimension(200, 16));
    filtersLabel.setIcon(Icons.FILTER);
    Font f = filtersLabel.getFont().deriveFont(Font.BOLD);
    filtersLabel.setFont(f);
    filtersPanel.add(filtersLabel, "wrap, growx, span");
    LogFilterValueChangeListener listener = new LogFilterValueChangeListener(table, sorter, filtersList, statusObserver);
    for (LogFilter filter : filtersList) {
      filter.init(new Properties(), dataTableModel);
      FilterPanel filterPanel = new FilterPanel(filter, listener);
      filtersPanel.add(filterPanel, "wrap, growx");
      if (filter instanceof ThreadFilter) {
        ThreadFilter threadFilter = (ThreadFilter) filter;
        focusOnThisThreadAction = new FocusOnThisThreadAction(threadFilter, filterPanel.getEnableCheckBox(), otrosApplication);
      } else if (filter instanceof TimeFilter) {
        focusOnEventsAfter = new FocusOnEventsAfter((TimeFilter) filter, filterPanel.getEnableCheckBox(), otrosApplication);
        focusOnEventsBefore = new FocusOnEventsBefore((TimeFilter) filter, filterPanel.getEnableCheckBox(), otrosApplication);
      } else if (filter instanceof ClassFilter) {
        focusOnSelectedClassesAction = new FocusOnSelectedClassesAction((ClassFilter) filter, filterPanel.getEnableCheckBox(), otrosApplication);
        ignoreSelectedEventsClasses = new IgnoreSelectedEventsClasses((ClassFilter) filter, filterPanel.getEnableCheckBox(), otrosApplication);
      } else if (filter instanceof LoggerNameFilter) {
        focusOnSelectedLoggerNameAction = new FocusOnSelectedLoggerNameAction((LoggerNameFilter) filter, filterPanel.getEnableCheckBox(), otrosApplication);
      } else if (filter instanceof CallHierarchyLogFilter) {
        showCallHierarchyAction = new ShowCallHierarchyAction((CallHierarchyLogFilter) filter, filterPanel.getEnableCheckBox(), otrosApplication);
      } else if (filter instanceof PropertyFilter) {
        propertyFilter = (PropertyFilter) filter;
        propertyFilterPanel = filterPanel;

      }
    }
    filtersLabel.add(logsMarkersPanel, "span, grow");
  }

  private JPopupMenu initTableContextMenu() {
    JPopupMenu menu = new JPopupMenu("Menu");
    JMenuItem mark = new JMenuItem("Mark selected rows");
    JPanel markPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    for (MarkerColors value : MarkerColors.values()) {
      markPanel.add(new JButton(Icons.DISK));
    }

    mark.addActionListener(new MarkRowAction(otrosApplication));
    JMenuItem unmark = new JMenuItem("Unmark selected rows");
    unmark.addActionListener(new UnMarkRowAction(otrosApplication));

    JMenuItem autoResizeMenu = new JMenu("Table auto resize mode");
    autoResizeMenu.setIcon(Icons.TABLE_RESIZE);
    JMenuItem autoResizeSubsequent = new JMenuItem("Subsequent columns");
    autoResizeSubsequent.addActionListener(new TableResizeActionListener(table, JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS));
    JMenuItem autoResizeLast = new JMenuItem("Last column");
    autoResizeLast.addActionListener(new TableResizeActionListener(table, JTable.AUTO_RESIZE_LAST_COLUMN));
    JMenuItem autoResizeNext = new JMenuItem("Next column");
    autoResizeNext.addActionListener(new TableResizeActionListener(table, JTable.AUTO_RESIZE_NEXT_COLUMN));
    JMenuItem autoResizeAll = new JMenuItem("All columns");
    autoResizeAll.addActionListener(new TableResizeActionListener(table, JTable.AUTO_RESIZE_ALL_COLUMNS));
    JMenuItem autoResizeOff = new JMenuItem("Auto resize off");
    autoResizeOff.addActionListener(new TableResizeActionListener(table, JTable.AUTO_RESIZE_OFF));
    autoResizeMenu.add(autoResizeSubsequent);
    autoResizeMenu.add(autoResizeOff);
    autoResizeMenu.add(autoResizeNext);
    autoResizeMenu.add(autoResizeLast);
    autoResizeMenu.add(autoResizeAll);
    JMenu removeMenu = new JMenu("Remove log events");
    removeMenu.setFont(menuLabelFont);
    removeMenu.setIcon(Icons.BIN);
    JLabel removeLabel = new JLabel("Remove by:");
    removeLabel.setFont(menuLabelFont);
    removeMenu.add(removeLabel);


    Map<String, Set<String>> propKeyValue = getPropertiesOfSelectedLogEvents();
    for (AcceptCondition acceptCondition : acceptConditionList) {
      removeMenu.add(new JMenuItem(new RemoveByAcceptanceCriteria(acceptCondition, otrosApplication)));
    }
    for (String propertyKey : propKeyValue.keySet()) {
      for (String propertyValue : propKeyValue.get(propertyKey)) {
        PropertyAcceptCondition propAcceptCondition = new PropertyAcceptCondition(propertyKey, propertyValue);
        removeMenu.add(new JMenuItem(new RemoveByAcceptanceCriteria(propAcceptCondition, otrosApplication)));
      }
    }


    menu.add(new JSeparator());
    JLabel labelMarkingRows = new JLabel("Marking/unmarking rows");
    labelMarkingRows.setFont(menuLabelFont);
    menu.add(labelMarkingRows);
    menu.add(new JSeparator());
    menu.add(mark);
    menu.add(markPanel);
    menu.add(unmark);
    JMenu[] markersMenu = getAutomaticMarkersMenu();
    menu.add(markersMenu[0]);
    menu.add(markersMenu[1]);
    menu.add(new ClearMarkingsAction(otrosApplication));
    menu.add(new JSeparator());
    JLabel labelQuickFilters = new JLabel("Quick filters");
    labelQuickFilters.setFont(menuLabelFont);
    menu.add(labelQuickFilters);
    menu.add(new JSeparator());
    menu.add(focusOnThisThreadAction);
    menu.add(focusOnEventsAfter);
    menu.add(focusOnEventsBefore);
    menu.add(focusOnSelectedClassesAction);
    menu.add(ignoreSelectedEventsClasses);
    menu.add(focusOnSelectedLoggerNameAction);
    menu.add(showCallHierarchyAction);
    for (String propertyKey : propKeyValue.keySet()) {
      for (String propertyValue : propKeyValue.get(propertyKey)) {
        menu.add(new FocusOnSelectedPropertyAction(propertyFilter, propertyFilterPanel.getEnableCheckBox(), otrosApplication, propertyKey, propertyValue));
      }
    }
    menu.add(new JSeparator());
    menu.add(removeMenu);
    menu.add(new JSeparator());
    JLabel labelTableOptions = new JLabel("Table options");
    labelTableOptions.setFont(menuLabelFont);
    menu.add(labelTableOptions);
    menu.add(new JSeparator());
    menu.add(autoResizeMenu);

    menu.add(new JSeparator());
    List<MenuActionProvider> menuActionProviders = otrosApplication.getLogViewPanelMenuActionProvider();
    for (MenuActionProvider menuActionProvider : menuActionProviders) {
      try {
        List<OtrosAction> actions = menuActionProvider.getActions(otrosApplication, this);
        if (actions == null) {
          continue;
        }
        for (OtrosAction action : actions) {
          menu.add(action);
        }
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Cant get action from from provider " + menuActionProvider, e);
      }
    }

    return menu;
  }

  public int[] getSelectedRowsInModel() {
    int[] selectedRows = table.getSelectedRows();
    for (int index = 0; index < selectedRows.length; index++) {
      selectedRows[index] = table.convertRowIndexToModel(selectedRows[index]);
    }
    return selectedRows;
  }

  private Map<String, Set<String>> getPropertiesOfSelectedLogEvents() {
    Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    int[] selectedRows = getSelectedRowsInModel();
    for (int i : selectedRows) {
      LogData logData = dataTableModel.getLogData(i);
      Map<String, String> properties = logData.getProperties();
      if (properties == null) {
        continue;
      }
      for (String s : properties.keySet()) {
        if (!result.containsKey(s)) {
          result.put(s, new TreeSet<String>());
        }
        result.get(s).add(properties.get(s));
      }

    }
    return result;
  }

  private JMenu[] getAutomaticMarkersMenu() {
    // AutomaticMarker[] markers = MarkersContainer.getInstance().getMarkers().toArray(new AutomaticMarker[0]);
    automaticMarkersMenu = new JMenu("Mark rows automatically");
    automaticMarkersMenu.setIcon(Icons.AUTOMATIC_MARKERS);
    automaticUnmarkersMenu = new JMenu("Unmark rows automatically");
    automaticUnmarkersMenu.setIcon(Icons.AUTOMATIC_UNMARKERS);
    updateMarkerMenu(markersContainer.getElements());
    return new JMenu[]{automaticMarkersMenu, automaticUnmarkersMenu};
  }

  private void addMarkerToMenu(JMenu menu, AutomaticMarker automaticMarker, HashMap<String, JMenu> marksGroups, boolean mode) {
    String[] groups = automaticMarker.getMarkerGroups();
    if (groups == null || groups.length == 0) {
      groups = new String[]{""};
    }
    for (String g : groups) {
      JMenuItem markerMenuItem = new JMenuItem(automaticMarker.getName());

      Icon icon = new ColorIcon(automaticMarker.getColors().getBackground(), automaticMarker.getColors().getForeground(), 16, 16);
      markerMenuItem.setIcon(icon);
      markerMenuItem.setToolTipText(automaticMarker.getDescription());
      markerMenuItem.addActionListener(new AutomaticMarkUnamrkActionListener(dataTableModel, automaticMarker, mode, statusObserver));
      if (g.length() > 0) {
        JMenu m = marksGroups.get(g);
        if (m == null) {
          m = new JMenu(g);
          marksGroups.put(g, m);
          menu.add(m);
        }
        m.add(markerMenuItem);
      } else {
        menu.add(markerMenuItem);
      }
    }

  }

  public void updateMarkerMenu(Collection<AutomaticMarker> markers) {
    HashMap<String, JMenu> marksGroups = new HashMap<String, JMenu>();
    HashMap<String, JMenu> unmarksGroups = new HashMap<String, JMenu>();

    automaticMarkersMenu.removeAll();
    automaticUnmarkersMenu.removeAll();
    for (AutomaticMarker automaticMarker : markers) {
      addMarkerToMenu(automaticMarkersMenu, automaticMarker, marksGroups, AutomaticMarkUnamrkActionListener.MODE_MARK);
      addMarkerToMenu(automaticUnmarkersMenu, automaticMarker, unmarksGroups, AutomaticMarkUnamrkActionListener.MODE_UNMARK);
    }
    GuiUtils.sortDirAndAlfabetic(automaticMarkersMenu);
    GuiUtils.sortDirAndAlfabetic(automaticUnmarkersMenu);

  }

  public void showOnlyThisColumns(TableColumns[] columns) {
    for (TableColumns tableColumns : columns) {
      table.getColumnExt(tableColumns).setVisible(true);
    }
    updateColumnsSize();
  }

  protected void initMessageDetailsToolbar() {
    final JButton buttonFormatters = new JButton("Message formatters", Icons.MESSAGE_FORMATTER);
    buttonFormatters.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        showMessageFormatterOrColorizerPopupMenu(e, "Message formatters", selectedMessageFormattersContainer, messageFormattersContainer);
      }
    });
    buttonFormatters.setToolTipText("Select used message formatters");
    messageDetailToolbar.add(buttonFormatters);


    final JButton buttonColorizers = new JButton("Message colorizers", Icons.MESSAGE_COLORIZER);
    buttonColorizers.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        showMessageFormatterOrColorizerPopupMenu(e, "Message colorizers", selectedMessageColorizersContainer, messageColorizersContainer);
      }
    });
    buttonColorizers.setToolTipText("Select used message colorizers");
    messageDetailToolbar.add(buttonColorizers);
    messageDetailToolbar.add(new CopyStyledMessageDetailAction(otrosApplication, dateFormat, selectedMessageColorizersContainer, selectedMessageFormattersContainer));

    messageDetailToolbar.add(new JLabel("Maximum message size for format"));


    final DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel(new String[]{});
    String[] values = new String[]{
        "10kB", "100kB", "200kB", "300kB", "400kB", "500kB", "600kB", "700kB", "800kB", "900kB", "1MB", "2MB", "3MB", "4MB", "5MB"
    };
    for (String value : values) {
      defaultComboBoxModel.addElement(value);
    }
    final JXComboBox messageMaximumSize = new JXComboBox(defaultComboBoxModel);
    messageMaximumSize.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String max = (String) defaultComboBoxModel.getElementAt(messageMaximumSize.getSelectedIndex());
        configuration.setProperty(ConfKeys.MESSAGE_FORMATTER_MAX_SIZE, max);
        messageDetailListener.setMaximumMessageSize((int) new FileSize(max).getBytes());

      }
    });


    messageMaximumSize.setEditable(false);
    AutoCompleteDecorator.decorate(messageMaximumSize);
    messageMaximumSize.setMaximumSize(new Dimension(100, 50));
    messageDetailToolbar.add(messageMaximumSize);
    String messageMaxSize = configuration.getString(ConfKeys.MESSAGE_FORMATTER_MAX_SIZE, (String) defaultComboBoxModel.getElementAt(messageMaximumSize.getSelectedIndex()));
    if (defaultComboBoxModel.getIndexOf(messageMaxSize) >= 0) {
      messageMaximumSize.setSelectedItem(messageMaxSize);
    }

  }

  private void showMessageFormatterOrColorizerPopupMenu(MouseEvent e, String menuTitle,
                                                        PluginableElementsContainer<? extends PluginableElement>
                                                            selectedPluginableElementsContainer, PluginableElementsContainer<? extends PluginableElement>
                                                            pluginableElementsContainer) {
    final JPopupMenu popupMenu = new JPopupMenu(menuTitle);
    popupMenu.add(new JLabel(menuTitle));
    ArrayList<PluginableElement> elements = new ArrayList<PluginableElement>(pluginableElementsContainer.getElements());
    Collections.sort(elements, new PluginableElementNameComparator());
    for (final PluginableElement pluginableElement : elements) {
      addMessageFormatterOrColorizerToMenu(popupMenu, pluginableElement, selectedPluginableElementsContainer);
    }
    popupMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void addMessageFormatterOrColorizerToMenu(final JPopupMenu menu, final PluginableElement pluginable,
                                                    final PluginableElementsContainer selectedPluginableContainer) {
    {
      final JCheckBoxMenuItem boxMenuItem = new JCheckBoxMenuItem(pluginable.getName(), selectedPluginableContainer.contains(pluginable));
      boxMenuItem.setToolTipText(pluginable.getDescription());
      menu.add(boxMenuItem);
      boxMenuItem.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          if (boxMenuItem.isSelected() && !selectedPluginableContainer.contains(pluginable)) {
            selectedPluginableContainer.addElement(pluginable);
          } else if (!boxMenuItem.isSelected() && selectedPluginableContainer.contains(pluginable)) {
            selectedPluginableContainer.removeElement(pluginable);
          }
        }
      });
    }
  }

  public JXTable getTable() {
    return table;
  }

  public LogDataTableModel getDataTableModel() {
    return dataTableModel;
  }

  public JPanel getLogsMarkersPanel() {
    return logsMarkersPanel;
  }

  @Override
  public int clear() {
    return dataTableModel.clear();
  }

  public LogData getDisplayedLogData() {
    return displayedLogData;
  }

  public void setDisplayedLogData(LogData displayedLogData) {
    this.displayedLogData = displayedLogData;
  }

  public OtrosJTextWithRulerScrollPane<JTextPane> getLogDetailWithRulerScrollPane() {
    return logDetailWithRulerScrollPane;
  }

  public PluginableElementsContainer<MessageColorizer> getSelectedMessageColorizersContainer() {
    return selectedMessageColorizersContainer;
  }

  public PluginableElementsContainer<MessageFormatter> getSelectedMessageFormattersContainer() {
    return selectedMessageFormattersContainer;
  }

  public JToolBar getMessageDetailToolbar() {
    return messageDetailToolbar;
  }

  private class MarkersMenuReloader implements PluginableElementEventListener<AutomaticMarker> {

    PluginableElementsContainer<AutomaticMarker> markersContainer = AllPluginables.getInstance().getMarkersContainser();

    @Override
    public void elementAdded(AutomaticMarker element) {
      updateMarkerMenu(markersContainer.getElements());

    }

    @Override
    public void elementRemoved(AutomaticMarker element) {
      updateMarkerMenu(markersContainer.getElements());
    }

    @Override
    public void elementChanged(AutomaticMarker element) {
      updateMarkerMenu(markersContainer.getElements());
    }

  }
}
