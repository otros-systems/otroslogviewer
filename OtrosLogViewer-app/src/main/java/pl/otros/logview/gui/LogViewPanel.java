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
import org.apache.commons.configuration.event.EventSource;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.accept.*;
import pl.otros.logview.api.*;
import pl.otros.logview.api.gui.*;
import pl.otros.logview.api.gui.NoteEvent.EventType;
import pl.otros.logview.api.model.*;
import pl.otros.logview.api.pluginable.*;
import pl.otros.logview.api.plugins.MenuActionProvider;
import pl.otros.logview.filter.*;
import pl.otros.logview.gui.actions.*;
import pl.otros.logview.gui.actions.table.MarkRowBySpaceKeyListener;
import pl.otros.logview.gui.config.LogTableFormatConfigView;
import pl.otros.logview.gui.message.update.MessageDetailListener;
import pl.otros.logview.gui.renderers.*;
import pl.otros.logview.gui.table.JTableWith2RowHighliting;
import pl.otros.logview.gui.util.JumpToCodeSelectionListener;
import pl.otros.logview.pluginable.PluginableElementNameComparator;
import pl.otros.logview.pluginable.SynchronizePluginableContainerListener;
import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;
import pl.otros.swing.rulerbar.RulerBarHelper;
import pl.otros.swing.table.ColumnLayout;
import pl.otros.swing.text.FontResize;
import pl.otros.swing.text.FullWidthJTextPane;
import pl.otros.vfs.browser.table.FileSize;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public class LogViewPanel extends LogViewPanelI {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogViewPanel.class.getName());
  private final OtrosJTextWithRulerScrollPane<JTextPane> logDetailWithRulerScrollPane;
  private final MessageDetailListener messageDetailListener;
  private final Font menuLabelFont;
  private final JPanel logsMarkersPanel;
  private JMenu automaticMarkersMenu;
  private JMenu automaticUnmarkersMenu;
  private final LogDataTableModel dataTableModel;
  private final OtrosApplication otrosApplication;
  private final FullWidthJTextPane logDetailTextArea;
  private final JXTable table;
  private final TableRowSorter<LogDataTableModel> sorter;
  private final StatusObserver statusObserver;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  //  private JScrollPane scrollPane;
  private FocusOnThisThreadAction focusOnThisThreadAction;
  private FocusOnEventsAfter focusOnEventsAfter;
  private FocusOnEventsBefore focusOnEventsBefore;
  private FocusOnSelectedClassesLikeAction focusOnSelectedClassesAction;
  private FocusOnSelectedClassesLikeAction focusOnSelectedLoggerNameAction;
  private IgnoreSelectedEventsClasses ignoreSelectedEventsClasses;
  private ShowCallHierarchyAction showCallHierarchyAction;
  private final PluginableElementsContainer<AutomaticMarker> markersContainer;
  private final PluginableElementsContainer<LogFilter> logFiltersContainer;
  private final PluginableElementsContainer<MessageColorizer> messageColorizersContainer;
  private final PluginableElementsContainer<MessageFormatter> messageFormattersContainer;
  private final PluginableElementsContainer<MessageColorizer> selectedMessageColorizersContainer;
  private final PluginableElementsContainer<MessageFormatter> selectedMessageFormattersContainer;
  private final JToolBar messageDetailToolbar;
  private List<AcceptCondition> acceptConditionList;
  private PropertyFilter propertyFilter;
  private FilterPanel propertyFilterPanel;
  private List<LogFilter> filtersList;
  private final DataConfiguration configuration;
  private LogData displayedLogData;

  public LogViewPanel(final LogDataTableModel dataTableModel, TableColumns[] visibleColumns, final OtrosApplication otrosApplication) {
    super();
    setName("LogViewPanel.panel");
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
    selectedMessageColorizersContainer = new PluginableElementsContainer<>();
    selectedMessageFormattersContainer = new PluginableElementsContainer<>();
    messageColorizersContainer.getElements().forEach(selectedMessageColorizersContainer::addElement);
    messageFormattersContainer.getElements().forEach(selectedMessageFormattersContainer::addElement);
    messageColorizersContainer.addListener(new SynchronizePluginableContainerListener<>(selectedMessageColorizersContainer));
    messageFormattersContainer.addListener(new SynchronizePluginableContainerListener<>(selectedMessageFormattersContainer));


    menuLabelFont = new JLabel().getFont().deriveFont(Font.BOLD);
    JPanel filtersPanel = new JPanel();
    JPanel logsTablePanel = new JPanel();
    logsMarkersPanel = new JPanel();
    JPanel leftPanel = new JPanel(new MigLayout());
    logDetailTextArea = new FullWidthJTextPane();
    logDetailTextArea.setFont(new Font("Courier New", 0, logDetailTextArea.getFont().getSize()));
    logDetailTextArea.setEditable(false);
    MouseAdapter locationInfo = new MessageDetailsMouseAdapter(otrosApplication, logDetailTextArea);
    logDetailTextArea.addMouseMotionListener(locationInfo);
    logDetailTextArea.addMouseListener(locationInfo);
    logDetailTextArea.setBorder(BorderFactory.createTitledBorder("Details"));
    logDetailWithRulerScrollPane = RulerBarHelper.wrapTextComponent((JTextPane) logDetailTextArea);
    FontResize.addFontResizeOnScroll(logDetailWithRulerScrollPane.getjScrollPane());
    table = new JTableWith2RowHighliting(dataTableModel);
    table.setName("LogViewPanel.log table");

    // Initialize default column visible before creating context menu
    table.setColumnControlVisible(true);
    final ColumnControlButton columnControlButton =
      new ColumnControlButton(table) {

        @Override
        public void togglePopup() {
          populatePopup();
          super.togglePopup();
        }

        @Override
        protected List<Action> getAdditionalActions() {
          final List<Action> additionalActions = super.getAdditionalActions();
          final AbstractAction saveLayout = new AbstractAction("Save current to new column layout", Icons.DISK) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
              String newLayoutName = JOptionPane.showInputDialog(table, "New Layout name");
              if (newLayoutName == null) {
                return;
              }
              newLayoutName = newLayoutName.trim();
              LOGGER.info(String.format("Saving New column layout '%s'", newLayoutName));
              ArrayList<String> visibleColNames = new ArrayList<>();
              for (TableColumn tc : table.getColumns()) {
                Object o = tc.getIdentifier();
                if (!(o instanceof TableColumns)) {
                  LOGGER.error("TableColumn identifier of unexpected type: " + tc.getIdentifier().getClass().getName());
                  LOGGER.warn("Throw up a pop-up");
                  return;
                }
                TableColumns tcs = (TableColumns) o;
                visibleColNames.add(tcs.getName());
              }
              ColumnLayout columnLayout = new ColumnLayout(newLayoutName, visibleColNames);
              final List<ColumnLayout> columnLayouts = LogTableFormatConfigView.loadColumnLayouts(configuration);
              columnLayouts.add(columnLayout);
              LogTableFormatConfigView.saveColumnLayouts(columnLayouts, configuration);
              populatePopup();
            }
          };
          additionalActions.add(saveLayout);

          final List<ColumnLayout> columnLayoutNames = LogTableFormatConfigView.loadColumnLayouts(configuration);
          for (final ColumnLayout columnLayout : columnLayoutNames) {
            final String name = columnLayout.getName();
            final AbstractAction applyColumnLayout = new ApplyColumnLayoutAction(name, Icons.EDIT_COLUMNS, columnLayout, table);
            additionalActions.add(applyColumnLayout);
          }
          return additionalActions;
        }
      };
    table.setColumnControl(columnControlButton);

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

    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    updateColumnsSize();
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    final Renderers renderers = Renderers.getInstance(otrosApplication);
    table.setDefaultRenderer(String.class, new TableMarkDecoratorRenderer(renderers.getStringRenderer()));
    table.setDefaultRenderer(Object.class, new TableMarkDecoratorRenderer(table.getDefaultRenderer(Object.class)));
    table.setDefaultRenderer(Integer.class, new TableMarkDecoratorRenderer(table.getDefaultRenderer(Object.class)));
    table.setDefaultRenderer(Level.class, new TableMarkDecoratorRenderer(renderers.getLevelRenderer()));
    table.setDefaultRenderer(Date.class, new TableMarkDecoratorRenderer(renderers.getDateRenderer()));
    final TimeDeltaRenderer timeDeltaRenderer = new TimeDeltaRenderer();
    table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
      final int[] selectedRows = table.getSelectedRows();
      if (selectedRows.length > 0) {
        final int selectedRow = selectedRows[selectedRows.length - 1];
        final Date selectedDate = dataTableModel.getLogData(table.convertRowIndexToModel(selectedRow)).getDate();
        timeDeltaRenderer.setSelectedTimestamp(selectedDate);
        table.repaint();
      }
    });
    table.setDefaultRenderer(TimeDelta.class, new TableMarkDecoratorRenderer(timeDeltaRenderer));

    ((EventSource) configuration.getConfiguration()).addConfigurationListener(ce -> {
      if (ce.getType() == AbstractConfiguration.EVENT_SET_PROPERTY && !ce.isBeforeUpdate()) {
        if (ce.getPropertyName().equals(ConfKeys.LOG_TABLE_FORMAT_DATE_FORMAT)) {
          table.setDefaultRenderer(Date.class, new TableMarkDecoratorRenderer(new DateRenderer(configuration.getString(ConfKeys.LOG_TABLE_FORMAT_DATE_FORMAT, "HH:mm:ss.SSS"))));
          updateTimeColumnSize();
        } else if (ce.getPropertyName().equals(ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER)) {
          table.setDefaultRenderer(Level.class, new TableMarkDecoratorRenderer(new LevelRenderer(configuration.get(LevelRenderer.Mode.class, ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly))));
          updateLevelColumnSize();
        }
      }
    });

    table.setDefaultRenderer(Boolean.class, new TableMarkDecoratorRenderer(table.getDefaultRenderer(Boolean.class)));
    table.setDefaultRenderer(Note.class, new TableMarkDecoratorRenderer(new NoteRenderer()));
    table.setDefaultRenderer(MarkerColors.class, new TableMarkDecoratorRenderer(new MarkTableRenderer()));
    table.setDefaultEditor(Note.class, new NoteTableEditor());
    table.setDefaultEditor(MarkerColors.class, new MarkTableEditor(otrosApplication));
    table.setDefaultRenderer(ClassWrapper.class, new TableMarkDecoratorRenderer(renderers.getClassWrapperRenderer()));
    sorter = new TableRowSorter<>(dataTableModel);
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

    table.getSelectionModel().addListSelectionListener(new JumpToCodeSelectionListener(otrosApplication, dataTableModel, table, 100));

    JTextArea notes = new JTextArea();
    notes.setEditable(false);
    NoteObserver allNotesObserver = new AllNotesTextAreaObserver(notes);
    dataTableModel.addNoteObserver(allNotesObserver);

    addFiltersGUIsToPanel(filtersPanel);
    logsTablePanel.setLayout(new BorderLayout());
    logsTablePanel.add(new JScrollPane(table));
    JPanel messageDetailsPanel = new JPanel(new BorderLayout());
    messageDetailToolbar = new JToolBar("MessageDetail");
    messageDetailsPanel.add(messageDetailToolbar, BorderLayout.NORTH);
    messageDetailsPanel.add(logDetailWithRulerScrollPane);
    initMessageDetailsToolbar();

    JTabbedPane jTabbedPane = new JTabbedPane();
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

    PopupListener popupListener = new PopupListener(this::initTableContextMenu);
    table.addMouseListener(popupListener);
    table.addKeyListener(popupListener);

    PopupListener popupListenerMessageDetailMenu = new PopupListener(this::initMessageDetailPopupMenu);
    logDetailTextArea.addMouseListener(popupListenerMessageDetailMenu);
    logDetailTextArea.addKeyListener(popupListenerMessageDetailMenu);

    dataTableModel.notifyAllNoteObservers(new NoteEvent(EventType.CLEAR, dataTableModel, null, 0));

    table.addKeyListener(new MarkRowBySpaceKeyListener(otrosApplication));
    initAcceptConditions();
  }

  private JPopupMenu initMessageDetailPopupMenu() {
    JPopupMenu jPopupMenu = new JPopupMenu();
    jPopupMenu.add(new CopySelectedText(otrosApplication, logDetailTextArea));
    jPopupMenu.add(new CopyStyledMessageDetailAction(otrosApplication, dateFormat, selectedMessageColorizersContainer, selectedMessageFormattersContainer));
    return jPopupMenu;
  }

  private void initAcceptConditions() {
    acceptConditionList = new ArrayList<>();
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
    updateColumnSizeIfVisible(TableColumns.DELTA, 60, 100);
    updateLevelColumnSize();
    updateColumnSizeIfVisible(TableColumns.CLASS, 100, 500);
    updateColumnSizeIfVisible(TableColumns.THREAD, 100, 300);
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
      default:
        updateColumnSizeIfVisible(TableColumns.LEVEL, levelWidth + 1, levelWidth + 1);
        break;
    }
  }

  private void updateColumnSizeIfVisible(TableColumns column, int width, int maxWidth) {
    table.getColumns(true).get(column.getColumn()).setMaxWidth(maxWidth);
    table.getColumns(true).get(column.getColumn()).setPreferredWidth(width);
  }

  @Override
  public JTextPane getLogDetailTextArea() {
    return logDetailTextArea;
  }

  @Override
  public void add(LogData[] autoResizeSubsequent) {
    dataTableModel.add(autoResizeSubsequent);

  }

  @Override
  public void add(LogData logData) {
    dataTableModel.add(logData);
  }

  @Override
  public LogData[] getLogData() {
    return dataTableModel.getLogData();
  }

  private void addFiltersGUIsToPanel(JPanel filtersPanel) {
    filtersPanel.setLayout(new MigLayout("", "[grow]", ""));

    Collection<LogFilter> loadedFilters = logFiltersContainer.getElements();

    // Reload filters, every instance of filter is connected to listeners, data table etc.
    filtersList = new ArrayList<>();
    for (LogFilter logFilter : loadedFilters) {
      try {
        LogFilter filter = logFilter.getClass().newInstance();
        filtersList.add(filter);
      } catch (Exception e) {
        LOGGER.error("Can't initialize filter: " + logFilter.getClass(), e);
      }
    }
    Collections.sort(filtersList, new Comparator<LogFilter>() {
      @Override
      public int compare(LogFilter o1, LogFilter o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
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
      } else if (filter instanceof ClassPackageFilter) {
        final ClassLikeFilter classLikeFilter = (ClassLikeFilter) filter;
        focusOnSelectedClassesAction = new FocusOnSelectedClassesLikeAction(classLikeFilter, "Focus on selected classes", filterPanel.getEnableCheckBox(), otrosApplication);
        ignoreSelectedEventsClasses = new IgnoreSelectedEventsClasses(classLikeFilter, filterPanel.getEnableCheckBox(), otrosApplication);
      } else if (filter instanceof LoggerHierarchyFilter) {
        final ClassLikeFilter classLikeFilter = (ClassLikeFilter) filter;
        focusOnSelectedLoggerNameAction = new FocusOnSelectedClassesLikeAction(classLikeFilter, "Focus on selected loggers", filterPanel.getEnableCheckBox(), otrosApplication);
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
        actions.forEach(menu::add);
      } catch (Exception e) {
        LOGGER.error("Cant get action from from provider " + menuActionProvider, e);
      }
    }

    return menu;
  }

  @Override
  public int[] getSelectedRowsInModel() {
    int[] selectedRows = table.getSelectedRows();
    for (int index = 0; index < selectedRows.length; index++) {
      selectedRows[index] = table.convertRowIndexToModel(selectedRows[index]);
    }
    return selectedRows;
  }

  private Map<String, Set<String>> getPropertiesOfSelectedLogEvents() {
    Map<String, Set<String>> result = new HashMap<>();
    int[] selectedRows = getSelectedRowsInModel();
    for (int i : selectedRows) {
      LogData logData = dataTableModel.getLogData(i);
      Map<String, String> properties = logData.getProperties();
      if (properties == null) {
        continue;
      }
      for (String s : properties.keySet()) {
        if (!result.containsKey(s)) {
          result.put(s, new TreeSet<>());
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

  @Override
  public void updateMarkerMenu(Collection<AutomaticMarker> markers) {
    HashMap<String, JMenu> marksGroups = new HashMap<>();
    HashMap<String, JMenu> unmarksGroups = new HashMap<>();

    automaticMarkersMenu.removeAll();
    automaticUnmarkersMenu.removeAll();
    for (AutomaticMarker automaticMarker : markers) {
      addMarkerToMenu(automaticMarkersMenu, automaticMarker, marksGroups, AutomaticMarkUnamrkActionListener.MODE_MARK);
      addMarkerToMenu(automaticUnmarkersMenu, automaticMarker, unmarksGroups, AutomaticMarkUnamrkActionListener.MODE_UNMARK);
    }
    GuiUtils.sortDirAndAlfabetic(automaticMarkersMenu);
    GuiUtils.sortDirAndAlfabetic(automaticUnmarkersMenu);

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
    String[] values = {
      "10kB", "100kB", "200kB", "300kB", "400kB", "500kB", "600kB", "700kB", "800kB", "900kB", "1MB", "2MB", "3MB", "4MB", "5MB"
    };
    for (String value : values) {
      defaultComboBoxModel.addElement(value);
    }
    final JXComboBox messageMaximumSize = new JXComboBox(defaultComboBoxModel);
    messageMaximumSize.addActionListener(e -> {
      String max = (String) defaultComboBoxModel.getElementAt(messageMaximumSize.getSelectedIndex());
      configuration.setProperty(ConfKeys.MESSAGE_FORMATTER_MAX_SIZE, max);
      messageDetailListener.setMaximumMessageSize((int) new FileSize(max).getBytes());

    });


    messageMaximumSize.setEditable(false);
    AutoCompleteDecorator.decorate(messageMaximumSize);
    messageMaximumSize.setMaximumSize(new Dimension(100, 50));
    messageDetailToolbar.add(messageMaximumSize);
    String messageMaxSize = configuration.getString(ConfKeys.MESSAGE_FORMATTER_MAX_SIZE, (String) defaultComboBoxModel.getElementAt(messageMaximumSize.getSelectedIndex()));
    if (defaultComboBoxModel.getIndexOf(messageMaxSize) >= 0) {
      messageMaximumSize.setSelectedItem(messageMaxSize);
    }

    final JCheckBox wrapText = new JCheckBox(Icons.SCROLL_HORIZONTAL);
    wrapText.addActionListener(e -> {
      logDetailTextArea.setFullWidth(wrapText.isSelected());
      final ImageIcon imageIcon = wrapText.isSelected() ? new ImageIcon(GrayFilter.createDisabledImage(Icons.SCROLL_HORIZONTAL.getImage())) : Icons.SCROLL_HORIZONTAL;
      wrapText.setIcon(imageIcon);
    });
    wrapText.setToolTipText("Enable/disable line wrapping");
    wrapText.setSelected(configuration.getBoolean(ConfKeys.MESSAGE_DETAIL_SCROLL_HORIZONTAL, true));
    wrapText.doClick();
    messageDetailToolbar.add(wrapText);

    messageDetailToolbar.add(FontResize.decreaseFontSizeAction(logDetailTextArea));
    messageDetailToolbar.add(FontResize.increaseFontSizeAction(logDetailTextArea));

  }

  private void showMessageFormatterOrColorizerPopupMenu(MouseEvent e, String menuTitle,
                                                        PluginableElementsContainer<? extends PluginableElement>
                                                          selectedPluginableElementsContainer, PluginableElementsContainer<? extends PluginableElement>
                                                          pluginableElementsContainer) {
    final JPopupMenu popupMenu = new JPopupMenu(menuTitle);
    popupMenu.add(new JLabel(menuTitle));
    ArrayList<PluginableElement> elements = new ArrayList<>(pluginableElementsContainer.getElements());
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
      boxMenuItem.addChangeListener(e -> {
        if (boxMenuItem.isSelected() && !selectedPluginableContainer.contains(pluginable)) {
          selectedPluginableContainer.addElement(pluginable);
        } else if (!boxMenuItem.isSelected() && selectedPluginableContainer.contains(pluginable)) {
          selectedPluginableContainer.removeElement(pluginable);
        }
      });
    }
  }

  @Override
  public JXTable getTable() {
    return table;
  }

  @Override
  public LogDataTableModel getDataTableModel() {
    return dataTableModel;
  }

  @Override
  public JPanel getLogsMarkersPanel() {
    return logsMarkersPanel;
  }

  @Override
  public int clear() {
    return dataTableModel.clear();
  }

  @Override
  public LogData getDisplayedLogData() {
    return displayedLogData;
  }

  @Override
  public void setDisplayedLogData(LogData displayedLogData) {
    this.displayedLogData = displayedLogData;
  }

  @Override
  public OtrosJTextWithRulerScrollPane<JTextPane> getLogDetailWithRulerScrollPane() {
    return logDetailWithRulerScrollPane;
  }

  @Override
  public PluginableElementsContainer<MessageColorizer> getSelectedMessageColorizersContainer() {
    return selectedMessageColorizersContainer;
  }

  @Override
  public PluginableElementsContainer<MessageFormatter> getSelectedMessageFormattersContainer() {
    return selectedMessageFormattersContainer;
  }

  @Override
  public JToolBar getMessageDetailToolbar() {
    return messageDetailToolbar;
  }

  private class MarkersMenuReloader implements PluginableElementEventListener<AutomaticMarker> {

    private PluginableElementsContainer<AutomaticMarker> markersContainer = AllPluginables.getInstance().getMarkersContainser();

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

  public OtrosApplication getOtrosApplication() {
    return otrosApplication;
  }
}
