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

package pl.otros.vfs.browser;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.vfs.browser.actions.*;
import pl.otros.vfs.browser.favorit.Favorite;
import pl.otros.vfs.browser.favorit.FavoritesUtils;
import pl.otros.vfs.browser.i18n.Messages;
import pl.otros.vfs.browser.list.MutableListDragListener;
import pl.otros.vfs.browser.list.MutableListDropHandler;
import pl.otros.vfs.browser.list.MutableListModel;
import pl.otros.vfs.browser.list.SelectFirstElementFocusAdapter;
import pl.otros.vfs.browser.listener.SelectionListener;
import pl.otros.vfs.browser.preview.PreviewComponent;
import pl.otros.vfs.browser.preview.PreviewListener;
import pl.otros.vfs.browser.table.*;
import pl.otros.vfs.browser.util.GuiUtils;
import pl.otros.vfs.browser.util.SwingUtils;
import pl.otros.vfs.browser.util.URIUtils;
import pl.otros.vfs.browser.util.VFSUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class VfsBrowser extends JPanel {


  public static final String MULTI_SELECTION_ENABLED_CHANGED_PROPERTY = "MultiSelectionEnabledChangedProperty";
  public static final String MULTI_SELECTION_MODE_CHANGED_PROPERTY = "SelectionModeChangedProperty";
  private static final Logger LOGGER = LoggerFactory.getLogger(VfsBrowser.class);
  private static final Icon COMPUTER_ICON = Icons.getInstance().getComputer();
  private static final String ACTION_GO_UP = "GO_UP";
  private static final String ACTION_OPEN = "OPEN";
  private static final Object ACTION_ADD_CURRENT_LOCATION_TO_FAVORITES = "ADD CURRENT LOCATION TO FAVORITES";
  private static final String ACTION_DELETE = "DELETE";
  private static final String ACTION_APPROVE = "ACTION APPROVE";
  private static final String ACTION_CANCEL = "ACTION CANCEL";
  private static final String ACTION_FOCUS_ON_TABLE = "FOCUS ON TABLE";
  private static final String ACTION_CLEAR_REGEX_FILTER = "CLEAR REGEX FILTER";
  private static final String ACTION_FOCUS_ON_REGEX_FILTER = "FOCUS ON REGEX FILTER";
  private static final String ACTION_EDIT = "EDIT";
  private static final String ACTION_SWITCH_SHOW_HIDDEN = "SWITCH SHOW HIDDEN";
  private static final String ACTION_FOCUS_ON_PATH = "FOCUS ON PATH";
  private static final String ACTION_REFRESH = "REFRESH";
  private static final String TABLE = "TABLE";
  private static final String LOADING = "LOADING";
  private final DataConfiguration configuration;
  private final SelectionListener[] listeners;
  private final URIUtils uriUtils;
  private JTextField pathField;
  private JTable tableFiles;
  private JScrollPane tableScrollPane;
  private JList favoritesUserList;
  private VfsTableModel vfsTableModel;
  private JPanel tablePanel;
  private PreviewComponent previewComponent;
  private JCheckBox showHidCheckBox;
  private JButton goUpButton;
  private JLabel statusLabel;
  private FileObject currentLocation;
  private CardLayout cardLayout;
  private MutableListModel<Favorite> favoritesUserListModel;
  private SelectionMode selectionMode = SelectionMode.DIRS_AND_FILES;
  private Action actionApproveDelegate;
  private Action actionCancelDelegate;
  private boolean multiSelectionEnabled = false;
  private JButton actionApproveButton;
  private JButton actionCancelButton;
  private JProgressBar loadingProgressBar;
  private JLabel loadingIconLabel;
  private TaskContext taskContext;
  private JToggleButton skipCheckingLinksButton;
  private JTextField filterField;
  private TableRowSorter<VfsTableModel> sorter;
  private boolean showHidden = false;
  private AbstractAction actionFocusOnTable;
  private boolean targetFileSelected;

  public VfsBrowser(SelectionListener... listeners) {
    this(new BaseConfiguration(), listeners);
  }

  public VfsBrowser(Configuration configuration, SelectionListener... listeners) {
    this(configuration, null, listeners);
  }

  public VfsBrowser(Configuration configuration, final String initialPath, SelectionListener... listeners) {
    super();
    this.configuration = new DataConfiguration(configuration);
    this.listeners = listeners;
    initGui(initialPath);
    VFSUtils.loadAuthStore();
    uriUtils = new URIUtils();
  }

  public void goToUrl(String url) {
    LOGGER.info("Going to URL by string: " + uriUtils.getFriendlyURI(url));
    FileObject resolveFile;
    try {
      resolveFile = VFSUtils.resolveFileObject(url);
      if (resolveFile != null) {
        LOGGER.info("URL: " + uriUtils.getFriendlyURI(url) + " is resolved ");
        goToFileObject(resolveFile);
      }
    } catch (FileSystemException e) {
      LOGGER.error("Can't go to URL " + uriUtils.getFriendlyURI(url), e);
      final String message = ExceptionsUtils.getRootCause(e).getClass().getName() + ": " + ExceptionsUtils.getRootCause(e).getLocalizedMessage();
      Runnable runnable = () -> JOptionPane.showMessageDialog(VfsBrowser.this, message,
        Messages.getMessage("browser.badlocation"),
        JOptionPane.ERROR_MESSAGE);
      SwingUtils.runInEdt(runnable);
    }


  }

  public void goToFileObject(final FileObject fileObject) {
    LOGGER.info("goToFileObject by fileobject");
    Arrays.stream(listeners).forEach(s -> s.enteredDir(fileObject));
    if (taskContext != null) {
      taskContext.setStop(true);
    }

    try {
      final FileObject[] files = VFSUtils.getFiles(fileObject);
      LOGGER.info("Have {} files in {}", files.length, fileObject.getName().getFriendlyURI());
      this.currentLocation = fileObject;

      taskContext = new TaskContext(Messages.getMessage("browser.checkingSFtpLinksTask"), files.length);
      taskContext.setIndeterminate(false);
      SwingWorker<Void, Void> refreshWorker = new SwingWorker<Void, Void>() {
        final Icon[] icons = new Icon[]{
          Icons.getInstance().getNetworkStatusOnline(),
          Icons.getInstance().getNetworkStatusAway(),
          Icons.getInstance().getNetworkStatusOffline()
        };
        int icon = 0;

        @Override
        protected void process(List<Void> chunks) {
          loadingProgressBar.setIndeterminate(taskContext.isIndeterminate());
          loadingProgressBar.setMaximum(taskContext.getMax());
          loadingProgressBar.setValue(taskContext.getCurrentProgress());
          loadingProgressBar.setString(String.format("%s [%d of %d]", taskContext.getName(), taskContext.getCurrentProgress(), taskContext.getMax()));
          loadingIconLabel.setIcon(icons[++icon % icons.length]);
        }

        @Override
        protected Void doInBackground() {
          try {
            while (!taskContext.isStop()) {
              publish();
              Thread.sleep(300);
            }
          } catch (InterruptedException ignore) {
            //ignore
          }
          return null;
        }
      };
      refreshWorker.execute();

      if (!skipCheckingLinksButton.isSelected()) {
        VFSUtils.checkForSftpLinks(files, taskContext);
      }
      taskContext.setStop(true);

      final FileObject[] fileObjectsWithParent = addParentToFiles(files);
      Runnable r = () -> {
        vfsTableModel.setContent(fileObjectsWithParent);
        try {
          pathField.setText(fileObject.getURL().toString());
        } catch (FileSystemException e) {
          LOGGER.error("Can't get URL", e);
        }
        if (tableFiles.getRowCount() > 0) {
          tableFiles.getSelectionModel().setSelectionInterval(0, 0);
        }
        updateStatusText();
      };
      SwingUtils.runInEdt(r);

    } catch (Exception e) {
      String url = null;
      if (fileObject != null && fileObject.getName() != null) {
        url = fileObject.getName().getFriendlyURI();
      }
      LOGGER.error("Can't go to URL for " + url, e);
      final String message = ExceptionsUtils.getRootCause(e).getClass().getName() + ": " + ExceptionsUtils.getRootCause(e).getLocalizedMessage();

      Runnable runnable = () -> JOptionPane.showMessageDialog(VfsBrowser.this, message,
        Messages.getMessage("browser.badlocation"),
        JOptionPane.ERROR_MESSAGE);
      SwingUtils.runInEdt(runnable);
    }
  }

  /**
   * Caller responsible for ensuring called from EDT.
   * We purposefully do not update UI other than indirectly reflecting
   * the selection.
   * Current use case is that we are finished with the browser window.
   */
  private void loadAndSelSingleFile(FileObject fileObject) throws FileSystemException {
    vfsTableModel.setContent(new ParentFileObject(fileObject.getParent()), fileObject);
    tableFiles.getSelectionModel().setSelectionInterval(1, 1);
  }

  private FileObject[] addParentToFiles(FileObject[] files) {
    FileObject[] newFiles = new FileObject[files.length + 1];
    try {
      FileObject parent = currentLocation.getParent();
      if (parent != null) {
        newFiles[0] = new ParentFileObject(parent);
        System.arraycopy(files, 0, newFiles, 1, files.length);
      } else {
        newFiles = files;
      }
    } catch (FileSystemException e) {
      LOGGER.warn("Can't add parent", e);
      newFiles = files;
    }
    return newFiles;
  }

  private void initGui(final String initialPath) {
    this.setLayout(new BorderLayout());
    JLabel pathLabel = new JLabel(Messages.getMessage("browser.location"));
    pathLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    pathField = new JTextField(80);
    pathField.setName("VfsBrowser.path");
    pathField.setFont(pathLabel.getFont().deriveFont(pathLabel.getFont().getSize() * 1.2f));
    pathField.setToolTipText(Messages.getMessage("nav.pathTooltip"));
    GuiUtils.addBlinkOnFocusGain(pathField);
    pathLabel.setLabelFor(pathField);
    pathLabel.setDisplayedMnemonic(Messages.getMessage("browser.location.mnemonic").charAt(0));

    InputMap inputMapPath = pathField.getInputMap(JComponent.WHEN_FOCUSED);
    inputMapPath.put(KeyStroke.getKeyStroke("ENTER"), "OPEN_PATH");
    inputMapPath.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), ACTION_FOCUS_ON_TABLE);
    pathField.getActionMap().put("OPEN_PATH", new BaseNavigateAction(this) {

      @Override
      protected void performLongOperation(CheckBeforeActionResult actionResult) {
        try {
          LOGGER.info("Open path {}", uriUtils.getFriendlyURI(pathField.getText()));
          SwingUtilities.invokeLater(() -> loadingProgressBar.setString("Resolving path"));
          final long start = System.currentTimeMillis();
          FileObject resolveFile = VFSUtils.resolveFileObject(pathField.getText().trim());
          LOGGER.info("Path resolved in " + (System.currentTimeMillis() - start) + "ms");
          LOGGER.info("Path {} resolved", uriUtils.getFriendlyURI(pathField.getText()));
          if (resolveFile != null && resolveFile.getType() == FileType.FILE) {
            LOGGER.info("Resolved path is a file");
            loadAndSelSingleFile(resolveFile);
            pathField.setText(resolveFile.getURL().toString());
            actionApproveDelegate.actionPerformed(
              // TODO:  Does actionResult provide an ID for 2nd param here,
              // or should use a Random number?
              new ActionEvent(actionResult, (int) new java.util.Date().getTime(), "SELECTED_FILE"));
          } else {
            goToFileObject(resolveFile);
          }
        } catch (FileSystemException fse) {
          LOGGER.error("Can't open path ", fse);
          SwingUtilities.invokeLater(() -> {
            loadingProgressBar.setString("Path can't be resolved");
            JOptionPane.showMessageDialog(VfsBrowser.this, "Can't open path: " + fse.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          });
        }
      }

      @Override
      protected boolean canGoUrl() {
        return true;
      }

      @Override
      protected boolean canExecuteDefaultAction() {
        return false;
      }

    });
    actionFocusOnTable = new

      AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          tableFiles.requestFocusInWindow();
          if (tableFiles.getSelectedRow() < 0 && tableFiles.getRowCount() == 0) {
            tableFiles.getSelectionModel().setSelectionInterval(0, 0);
          }
        }
      };
    pathField.getActionMap().put(ACTION_FOCUS_ON_TABLE, actionFocusOnTable);

    BaseNavigateActionGoUp goUpAction = new BaseNavigateActionGoUp(this);
    goUpButton = new JButton(goUpAction);

    BaseNavigateActionRefresh refreshAction = new BaseNavigateActionRefresh(this);
    JButton refreshButton = new JButton(refreshAction);
    refreshButton.setName("VfsBrowser.refresh");

    JToolBar upperPanel = new JToolBar(Messages.getMessage("nav.ToolBarName"));
    upperPanel.setRollover(true);
    upperPanel.add(pathLabel);
    upperPanel.add(pathField, "growx");
    upperPanel.add(goUpButton);
    upperPanel.add(refreshButton);

    AddCurrentLocationToFavoriteAction addCurrentLocationToFavoriteAction = new AddCurrentLocationToFavoriteAction(this);
    JButton addCurrentLocationToFavoriteButton = new JButton(addCurrentLocationToFavoriteAction);
    addCurrentLocationToFavoriteButton.setText("");
    upperPanel.add(addCurrentLocationToFavoriteButton);

    previewComponent = new PreviewComponent();

    vfsTableModel = new VfsTableModel();

    tableFiles = new JTable(vfsTableModel);
    tableFiles.setFillsViewportHeight(true);
    tableFiles.getColumnModel().getColumn(0).setMinWidth(140);
    tableFiles.getColumnModel().getColumn(1).setMaxWidth(80);
    tableFiles.getColumnModel().getColumn(2).setMaxWidth(80);
    tableFiles.getColumnModel().getColumn(3).setMaxWidth(180);
    tableFiles.getColumnModel().getColumn(3).setMinWidth(120);


    sorter = new TableRowSorter<>(vfsTableModel);
    final FileNameWithTypeComparator fileNameWithTypeComparator = new FileNameWithTypeComparator();
    sorter.addRowSorterListener(e -> {
      RowSorterEvent.Type type = e.getType();
      if (type.equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
        List<? extends RowSorter.SortKey> sortKeys = e.getSource().getSortKeys();
        for (RowSorter.SortKey sortKey : sortKeys) {
          if (sortKey.getColumn() == VfsTableModel.COLUMN_NAME) {
            fileNameWithTypeComparator.setSortOrder(sortKey.getSortOrder());
          }
        }
      }
    });
    sorter.setComparator(VfsTableModel.COLUMN_NAME, fileNameWithTypeComparator);

    tableFiles.setRowSorter(sorter);
    tableFiles.setShowGrid(false);
    tableFiles.getSelectionModel().addListSelectionListener(e -> {
      try {
        selectionChanged();
      } catch (FileSystemException e1) {
        LOGGER.error("Error during update state", e);
      }
    });
    tableFiles.setColumnSelectionAllowed(false);
    vfsTableModel.addTableModelListener(e -> updateStatusText());


    tableFiles.setDefaultRenderer(FileSize.class, new FileSizeTableCellRenderer());
    tableFiles.setDefaultRenderer(FileNameWithType.class, new FileNameWithTypeTableCellRenderer());
    tableFiles.setDefaultRenderer(Date.class, new MixedDateTableCellRenderer());
    tableFiles.setDefaultRenderer(FileType.class, new FileTypeTableCellRenderer());

    tableFiles.getSelectionModel().addListSelectionListener(new PreviewListener(this, previewComponent, listeners));
    tableFiles.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
          Arrays.stream(listeners).forEach(s -> s.selectedItem(getSelectedFiles()));
        }
      }
    );

    JPanel favoritesPanel = new JPanel(new MigLayout("wrap, fillx", "[grow]"));
    favoritesUserListModel = new MutableListModel<>();

    List<Favorite> favSystemLocations = FavoritesUtils.getSystemLocations();
    List<Favorite> favUser = FavoritesUtils.loadFromProperties(configuration);
    List<Favorite> favJVfsFileChooser = FavoritesUtils.getJvfsFileChooserBookmarks();
    for (Favorite favorite : favUser) {
      favoritesUserListModel.add(favorite);
    }
    favoritesUserListModel.addListDataListener(new ListDataListener() {
      @Override
      public void intervalAdded(ListDataEvent e) {
        saveFavorites();

      }

      @Override
      public void intervalRemoved(ListDataEvent e) {
        saveFavorites();
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
        saveFavorites();
      }

      protected void saveFavorites() {
        FavoritesUtils.storeFavorites(configuration, favoritesUserListModel.getList());
      }
    });


    favoritesUserList = new JList(favoritesUserListModel);
    favoritesUserList.setTransferHandler(new MutableListDropHandler(favoritesUserList));
    new MutableListDragListener(favoritesUserList);
    favoritesUserList.setCellRenderer(new FavoriteListCellRenderer());
    favoritesUserList.addFocusListener(new SelectFirstElementFocusAdapter());

    addOpenActionToList(favoritesUserList);
    addEditActionToList(favoritesUserList, favoritesUserListModel);

    favoritesUserList.getActionMap().put(ACTION_DELETE, new AbstractAction(Messages.getMessage("favorites.deleteButtonText"),
      Icons.getInstance().getMinusButton()) {

      @Override
      public void actionPerformed(ActionEvent e) {
        Favorite favorite = favoritesUserListModel.getElementAt(favoritesUserList.getSelectedIndex());
        if (!Favorite.Type.USER.equals(favorite.getType())) {
          return;
        }
        int response = JOptionPane.showConfirmDialog(VfsBrowser.this, Messages.getMessage("favorites.areYouSureToDeleteConnections"),
          Messages.getMessage("favorites.confirm"),
          JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
          favoritesUserListModel.remove(favoritesUserList.getSelectedIndex());
        }
      }
    });
    InputMap favoritesListInputMap = favoritesUserList.getInputMap(JComponent.WHEN_FOCUSED);
    favoritesListInputMap.put(KeyStroke.getKeyStroke("DELETE"), ACTION_DELETE);


    ActionMap actionMap = tableFiles.getActionMap();
    actionMap.put(ACTION_OPEN, new BaseNavigateActionOpen(this));
    actionMap.put(ACTION_GO_UP, goUpAction);
    actionMap.put(ACTION_APPROVE, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (actionApproveButton.isEnabled()) {
          actionApproveDelegate.actionPerformed(e);
        }
      }
    });

    InputMap inputMap = tableFiles.getInputMap(JComponent.WHEN_FOCUSED);
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), ACTION_OPEN);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), ACTION_APPROVE);

    inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), ACTION_GO_UP);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), ACTION_GO_UP);
    addPopupMenu(favoritesUserList, ACTION_OPEN, ACTION_EDIT, ACTION_DELETE);

    JList favoriteSystemList = new JList(new Vector<Object>(favSystemLocations));
    favoriteSystemList.setCellRenderer(new FavoriteListCellRenderer());
    addOpenActionToList(favoriteSystemList);
    addPopupMenu(favoriteSystemList, ACTION_OPEN);
    favoriteSystemList.addFocusListener(new SelectFirstElementFocusAdapter());

    JList favoriteJVfsList = new JList(new Vector<Object>(favJVfsFileChooser));
    addOpenActionToList(favoriteJVfsList);
    favoriteJVfsList.setCellRenderer(new FavoriteListCellRenderer());
    addPopupMenu(favoriteJVfsList, ACTION_OPEN);
    favoriteJVfsList.addFocusListener(new SelectFirstElementFocusAdapter());


    JLabel favoritesSystemLocationsLabel = getTitleListLabel(Messages.getMessage("favorites.systemLocations"), COMPUTER_ICON);
    favoritesSystemLocationsLabel.setLabelFor(favoriteSystemList);
    favoritesSystemLocationsLabel.setDisplayedMnemonic(Messages.getMessage("favorites.systemLocations.mnemonic").charAt(0));
    favoritesPanel.add(favoritesSystemLocationsLabel, "gapleft 16");
    favoritesPanel.add(favoriteSystemList, "growx");
    JLabel favoritesFavoritesLabel = getTitleListLabel(Messages.getMessage("favorites.favorites"), Icons.getInstance().getStar());
    favoritesFavoritesLabel.setLabelFor(favoritesUserList);
    favoritesFavoritesLabel.setDisplayedMnemonic(Messages.getMessage("favorites.favorites.mnemonic").charAt(0));
    favoritesPanel.add(favoritesFavoritesLabel, "gapleft 16");
    favoritesPanel.add(favoritesUserList, "growx");

    if (favoriteJVfsList.getModel().getSize() > 0) {
      JLabel favoritesJVfsFileChooser = getTitleListLabel(Messages.getMessage("favorites.JVfsFileChooserBookmarks"), null);
      favoritesJVfsFileChooser.setDisplayedMnemonic(Messages.getMessage("favorites.JVfsFileChooserBookmarks.mnemonic").charAt(0));
      favoritesJVfsFileChooser.setLabelFor(favoriteJVfsList);
      favoritesPanel.add(favoritesJVfsFileChooser, "gapleft 16");
      favoritesPanel.add(favoriteJVfsList, "growx");
    }


    tableFiles.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          tableFiles.getActionMap().get(ACTION_OPEN).actionPerformed(null);
        }
      }
    });
    tableFiles.addKeyListener(new QuickSearchKeyAdapter());


    cardLayout = new CardLayout();
    tablePanel = new JPanel(cardLayout);
    loadingProgressBar = new JProgressBar();
    loadingProgressBar.setStringPainted(true);
    loadingProgressBar.setString(Messages.getMessage("browser.loading"));
    loadingProgressBar.setIndeterminate(true);
    loadingIconLabel = new JLabel(Icons.getInstance().getNetworkStatusOnline());
    skipCheckingLinksButton = new JToggleButton(Messages.getMessage("browser.skipCheckingLinks"));
    skipCheckingLinksButton.addActionListener(actionEvent -> {
      if (taskContext != null) {
        taskContext.setStop(skipCheckingLinksButton.isSelected());
      }
    });


    showHidCheckBox = new JCheckBox(Messages.getMessage("browser.showHidden.label"), showHidden);
    showHidCheckBox.setToolTipText(Messages.getMessage("browser.showHidden.tooltip"));
    showHidCheckBox.setMnemonic(Messages.getMessage("browser.showHidden.mnemonic").charAt(0));
    Font tmpFont = showHidCheckBox.getFont();
    showHidCheckBox.setFont(tmpFont.deriveFont(tmpFont.getSize() * 0.9f));
    showHidCheckBox.addActionListener(e -> updateUiFilters());

    final String defaultFilterText = Messages.getMessage("browser.nameFilter.defaultText");
    filterField = new JTextField("", 16);
    filterField.setName("VfsBrowser.filter");
    filterField.setForeground(filterField.getDisabledTextColor());
    filterField.setToolTipText(Messages.getMessage("browser.nameFilter.tooltip"));
    PromptSupport.setPrompt(defaultFilterText, filterField);
    filterField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        documentChanged();
      }

      void documentChanged() {
        if (filterField.getText().length() == 0) {
          updateUiFilters();
        }
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        documentChanged();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        documentChanged();
      }
    });
    filterField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        updateUiFilters();
      }
    });

    AbstractAction actionClearRegexFilter = new

      AbstractAction(Messages.getMessage("browser.nameFilter.clearFilterText")) {

        @Override
        public void actionPerformed(ActionEvent e) {
          filterField.setText("");
        }
      };
    filterField.getActionMap().put(ACTION_FOCUS_ON_TABLE, actionFocusOnTable);
    filterField.getActionMap().put(ACTION_CLEAR_REGEX_FILTER, actionClearRegexFilter);

    filterField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), ACTION_FOCUS_ON_TABLE);
    filterField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), ACTION_FOCUS_ON_TABLE);
    filterField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), ACTION_FOCUS_ON_TABLE);
    filterField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_FOCUS_ON_TABLE);
    filterField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_CLEAR_REGEX_FILTER);


    JLabel nameFilterLabel = new JLabel(Messages.getMessage("browser.nameFilter"));
    nameFilterLabel.setLabelFor(filterField);
    nameFilterLabel.setDisplayedMnemonic(Messages.getMessage("browser.nameFilter.mnemonic").charAt(0));

    sorter.setRowFilter(createFilter());
    statusLabel = new JLabel();

    JPanel listenerPanel = new JPanel(new BorderLayout());


    actionApproveButton = new JButton();
    actionApproveButton.setFont(actionApproveButton.getFont().deriveFont(Font.BOLD));
    actionCancelButton = new JButton();

    ActionMap browserActionMap = this.getActionMap();
    browserActionMap.put(ACTION_FOCUS_ON_REGEX_FILTER, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        filterField.requestFocus();
        filterField.selectAll();
        GuiUtils.blinkComponent(filterField);
      }
    });

    browserActionMap.put(ACTION_FOCUS_ON_PATH, new SetFocusOnAction(pathField));
    browserActionMap.put(ACTION_SWITCH_SHOW_HIDDEN, new ClickOnJComponentAction(showHidCheckBox));
    browserActionMap.put(ACTION_REFRESH, refreshAction);
    browserActionMap.put(ACTION_ADD_CURRENT_LOCATION_TO_FAVORITES, addCurrentLocationToFavoriteAction);
    browserActionMap.put(ACTION_GO_UP, goUpAction);
    browserActionMap.put(ACTION_FOCUS_ON_TABLE, new SetFocusOnAction(tableFiles));
    browserActionMap.put(ACTION_CANCEL, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionCancelDelegate.actionPerformed(e);
      }
    });

    InputMap browserInputMap = this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    browserInputMap.put(KeyStroke.getKeyStroke("control F"), ACTION_FOCUS_ON_REGEX_FILTER);
    browserInputMap.put(KeyStroke.getKeyStroke("control L"), ACTION_FOCUS_ON_PATH);
    browserInputMap.put(KeyStroke.getKeyStroke("F4"), ACTION_FOCUS_ON_PATH);
    browserInputMap.put(KeyStroke.getKeyStroke("control H"), ACTION_SWITCH_SHOW_HIDDEN);
    browserInputMap.put(KeyStroke.getKeyStroke("control R"), ACTION_REFRESH);
    browserInputMap.put(KeyStroke.getKeyStroke("F5"), ACTION_REFRESH);
    browserInputMap.put(KeyStroke.getKeyStroke("control D"), ACTION_ADD_CURRENT_LOCATION_TO_FAVORITES);
    browserInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK), ACTION_GO_UP);
    browserInputMap.put(KeyStroke.getKeyStroke("control T"), ACTION_FOCUS_ON_TABLE);
    browserInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_CANCEL);

    //DO layout
    // create the layer for the panel using our custom layerUI
    tableScrollPane = new JScrollPane(tableFiles);

    JPanel tableScrollPaneWithFilter = new JPanel(new BorderLayout());
    tableScrollPaneWithFilter.add(tableScrollPane);
    JToolBar filtersToolbar = new JToolBar("Filters");
    filtersToolbar.setFloatable(false);
    filtersToolbar.setBorderPainted(true);
    tableScrollPaneWithFilter.add(filtersToolbar, BorderLayout.SOUTH);
    filtersToolbar.add(nameFilterLabel);
    filtersToolbar.add(filterField);
    filtersToolbar.add(showHidCheckBox);
    JSplitPane tableWithPreviewPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tableScrollPaneWithFilter, previewComponent);
    tableWithPreviewPane.setOneTouchExpandable(true);


    JPanel loadingPanel = new JPanel(new MigLayout());
    loadingPanel.add(loadingIconLabel, "right");
    loadingPanel.add(loadingProgressBar, "left, w 420:420:500,wrap");
    loadingPanel.add(skipCheckingLinksButton, "span, right");
    tablePanel.add(loadingPanel, LOADING);
    tablePanel.add(tableWithPreviewPane, TABLE);


    JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(favoritesPanel), tablePanel);
    jSplitPane.setOneTouchExpandable(true);
    jSplitPane.setDividerLocation(180);


    JPanel southPanel = new JPanel(
      new MigLayout("", "[]push[][]", ""));
    southPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    southPanel.add(statusLabel);
    southPanel.add(listenerPanel);
    southPanel.add(actionApproveButton);
    southPanel.add(actionCancelButton);

    this.add(upperPanel, BorderLayout.NORTH);
    this.add(jSplitPane, BorderLayout.CENTER);
    this.add(southPanel, BorderLayout.SOUTH);

    Arrays.stream(listeners)
      .map(SelectionListener::getView)
      .forEach(listenerPanel::add);

    try {
      selectionChanged();
    } catch (FileSystemException e) {
      LOGGER.error("Can't initialize default selection mode", e);
    }
    // Why this not done in EDT?
    // Is it assume that constructor is invoked from an  EDT?
    try {
      if (initialPath == null) {
        goToFileObject(VFSUtils.getUserHome());
      } else {
        try {
          FileObject resolveFile = VFSUtils.resolveFileObject(initialPath);
          if (resolveFile != null && resolveFile.getType() == FileType.FILE) {
            loadAndSelSingleFile(resolveFile);
            pathField.setText(resolveFile.getURL().toString());
            targetFileSelected = true;
            return;
          }
        } catch (FileSystemException fse) {
          // Intentionally empty
        }
        goToUrl(initialPath);
      }
    } catch (FileSystemException e1) {
      LOGGER.error("Can't initialize default location", e1);
    }

    showTable();
  }

  private void updateStatusText() {
    int tableFilesRowCount = tableFiles.getRowCount() - 1;
    int modelCount = vfsTableModel.getRowCount() - 1;
    statusLabel.setText(Messages.getMessage("browser.folderContainsXElementsShowingY", modelCount, tableFilesRowCount));
  }

  private void updateUiFilters() {
    showHidden = showHidCheckBox.isSelected();
    sorter.setRowFilter(createFilter());
    updateStatusText();
  }

  private RowFilter<VfsTableModel, Integer> createFilter() {
    RowFilter<VfsTableModel, Integer> regexFilter = new VfsTableModelFileNameRowFilter(filterField);
    RowFilter<VfsTableModel, Integer> hiddenFilter = new VfsTableModelHiddenFileRowFilter(showHidden);
    RowFilter<VfsTableModel, Integer> alwaysShowParent = new VfsTableModelShowParentRowFilter();
    @SuppressWarnings("unchecked") RowFilter<VfsTableModel, Integer> filters = RowFilter.andFilter(Arrays.asList(regexFilter, hiddenFilter));
    filters = RowFilter.orFilter(Arrays.asList(filters, alwaysShowParent));
    return filters;
  }

  private JLabel getTitleListLabel(String text, Icon icon) {
    JLabel jLabel = new JLabel(text, icon, SwingConstants.CENTER);
    Font font = jLabel.getFont();
    jLabel.setFont(font.deriveFont(Font.ITALIC | Font.BOLD, font.getSize() * 1.1f));
    jLabel.setBorder(BorderFactory.createEmptyBorder(10, 3, 0, 3));

    return jLabel;
  }

  private JPopupMenu addPopupMenu(JList list, String... actions) {
    JPopupMenu favoritesPopupMenu = new JPopupMenu();
    for (String action : actions) {
      favoritesPopupMenu.add(list.getActionMap().get(action));
    }
    list.addKeyListener(new PopupListener(favoritesPopupMenu));
    list.addMouseListener(new PopupListener(favoritesPopupMenu));
    return favoritesPopupMenu;
  }

  private void addOpenActionToList(final JList favoritesList) {
    favoritesList.getActionMap().put(ACTION_OPEN, new OpenSelectedFavorite(this, favoritesList));
    favoritesList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          favoritesList.getActionMap().get(ACTION_OPEN).actionPerformed(null);
        }
      }
    });
    InputMap favoritesListInputMap = favoritesList.getInputMap(JComponent.WHEN_FOCUSED);
    favoritesListInputMap.put(KeyStroke.getKeyStroke("ENTER"), ACTION_OPEN);
  }

  private void addEditActionToList(final JList favoritesList, final MutableListModel<Favorite> listModel) {
    favoritesList.getActionMap().put(ACTION_EDIT, new EditFavorite(favoritesList, listModel));

    InputMap favoritesListInputMap = favoritesList.getInputMap(JComponent.WHEN_FOCUSED);
    favoritesListInputMap.put(KeyStroke.getKeyStroke("F2"), ACTION_EDIT);
  }

  private void selectionChanged() throws FileSystemException {
    LOGGER.debug("Updating selection");
    boolean acceptEnabled = false;
    if (getSelectedFiles().length == 0) {
      acceptEnabled = false;
    } else if (isMultiSelectionEnabled()) {
      boolean filesSelected = false;
      boolean folderSelected = false;

      for (FileObject fo : getSelectedFiles()) {
        FileType fileType = fo.getType();
        if (fileType == FileType.FILE) {
          filesSelected = true;
        } else if (fileType == FileType.FOLDER) {
          folderSelected = true;
        }
      }
      if (selectionMode == SelectionMode.FILES_ONLY && filesSelected && !folderSelected) {
        acceptEnabled = true;
      } else if (selectionMode == SelectionMode.DIRS_ONLY && !filesSelected && folderSelected) {
        acceptEnabled = true;
      } else if (selectionMode == SelectionMode.DIRS_AND_FILES) {
        acceptEnabled = true;
      }
    } else {
      FileObject selectedFileObject = getSelectedFileObject();
      FileType type = selectedFileObject.getType();
      if (selectionMode == SelectionMode.FILES_ONLY && type == FileType.FILE ||
        selectionMode == SelectionMode.DIRS_ONLY && type == FileType.FOLDER) {
        acceptEnabled = true;
      } else if (SelectionMode.DIRS_AND_FILES == selectionMode) {
        acceptEnabled = true;
      }
    }

    if (actionApproveDelegate != null) {
      actionApproveDelegate.setEnabled(acceptEnabled);
    }
    actionApproveButton.setEnabled(acceptEnabled);
  }

  public FileObject getCurrentLocation() {
    return currentLocation;
  }

  public MutableListModel getFavoritesUserListModel() {
    return favoritesUserListModel;
  }

  public void showLoading() {
    LOGGER.debug("Showing loading panel");
    loadingProgressBar.setIndeterminate(true);
    loadingProgressBar.setString(Messages.getMessage("browser.loading..."));
    skipCheckingLinksButton.setSelected(false);
    cardLayout.show(tablePanel, LOADING);
  }

  public void showTable() {
    LOGGER.debug("Showing result table");
    tableScrollPane.getVerticalScrollBar().setValue(0);
    cardLayout.show(tablePanel, TABLE);
  }

  public boolean isMultiSelectionEnabled() {
    return multiSelectionEnabled;
  }

  public void setMultiSelectionEnabled(boolean b) {
    int selectionMode = b ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION;
    tableFiles.getSelectionModel().setSelectionMode(selectionMode);
    if (multiSelectionEnabled == b) {
      return;
    }
    boolean oldValue = multiSelectionEnabled;
    multiSelectionEnabled = b;
    firePropertyChange(MULTI_SELECTION_ENABLED_CHANGED_PROPERTY, oldValue, multiSelectionEnabled);
    try {
      selectionChanged();
    } catch (FileSystemException e) {
      LOGGER.error("Error during update state", e);
    }
  }

  public SelectionMode getSelectionMode() {
    return selectionMode;
  }

  public void setSelectionMode(SelectionMode mode) {
    if (selectionMode == mode) {
      return;
    }
    SelectionMode oldValue = selectionMode;
    this.selectionMode = mode;
    firePropertyChange(MULTI_SELECTION_MODE_CHANGED_PROPERTY, oldValue, selectionMode);
    try {
      selectionChanged();
    } catch (FileSystemException e) {
      LOGGER.error("Error during update state", e);
    }
  }

  public void setApproveAction(Action action) {
    actionApproveDelegate = action;
    actionApproveButton.setAction(actionApproveDelegate);
    actionApproveButton.setName("VfsBrowser.open");
    actionApproveButton.setText(String.format("%s [Ctrl+Enter]", actionApproveDelegate.getValue(Action.NAME)));
    if (targetFileSelected) {
      actionApproveDelegate.actionPerformed(
        // TODO:  Does actionResult provide an ID for 2nd param here,
        // or should use a Random number?
        new ActionEvent(action, (int) new Date().getTime(), "SELECTED_FILE"));
    }
  }

  public void setCancelAction(Action cancelAction) {
    actionCancelDelegate = cancelAction;
    actionCancelButton.setAction(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showTable();
        cancelAction.actionPerformed(e);
      }

      @Override
      public Object getValue(String key) {
        return cancelAction.getValue(key);
      }
    });
    try {
      selectionChanged();
    } catch (FileSystemException e) {
      LOGGER.warn("Problem with checking selection conditions", e);
    }

  }

  public FileObject getSelectedFileObject() {
    int selectedRow = tableFiles.getSelectedRow();
    if (selectedRow > -1) {
      return vfsTableModel.get(tableFiles.convertRowIndexToModel(selectedRow));
    }
    return null;
  }

  public FileObject[] getSelectedFiles() {
    int[] selectedRows = tableFiles.getSelectedRows();
    FileObject[] fileObjects = new FileObject[selectedRows.length];
    for (int i = 0; i < selectedRows.length; i++) {
      fileObjects[i] = vfsTableModel.get(tableFiles.convertRowIndexToModel(selectedRows[i]));
    }
    return fileObjects;
  }

  public void selectNextFileStarting(String string) {
    LOGGER.debug("Looking for file starting with {}", string);
    int selectedRow = tableFiles.getSelectedRow();
    selectedRow = selectedRow < 0 ? 0 : selectedRow;
    LOGGER.debug("Starting search with row {}", selectedRow);
    boolean fullLoop;
    int started = selectedRow;
    do {
      LOGGER.debug("Checking table row {}", selectedRow);
      int convertRowIndexToModel = tableFiles.convertRowIndexToModel(selectedRow);
      LOGGER.debug("Table row {} is row {} from model", selectedRow, convertRowIndexToModel);
      FileObject fileObject = vfsTableModel.get(convertRowIndexToModel);
      LOGGER.debug("Checking {} if begins with {}", fileObject.getName().getBaseName(), string);
      if (fileObject.getName().getBaseName().toLowerCase().startsWith(string.toLowerCase())) {
        tableFiles.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        tableFiles.scrollRectToVisible(new Rectangle(tableFiles.getCellRect(selectedRow, 0, true)));
        break;
      }
      selectedRow++;
      selectedRow = selectedRow >= tableFiles.getRowCount() ? 0 : selectedRow;
      fullLoop = selectedRow == started;
    } while (!fullLoop);
  }

  private final class QuickSearchKeyAdapter extends KeyAdapter {

    private static final String LETTERS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    private static final String DIGITS = "0123456789";
    private static final String OTHER_CHARS = "!@#$%^&*()()-_=+[];:'\",./ ";
    private static final String ALLOWED_CHARS = LETTERS + DIGITS + OTHER_CHARS;
    private final long typeTimeout = 500;
    private final StringBuilder sb;
    private long lastTimeTyped = 0;

    public QuickSearchKeyAdapter() {
      sb = new StringBuilder();
    }

    @Override
    public void keyTyped(KeyEvent e) {
      char keyChar = e.getKeyChar();
      if (ALLOWED_CHARS.indexOf(keyChar) > -1) {
        if (System.currentTimeMillis() > lastTimeTyped + typeTimeout) {
          sb.setLength(0);
        }
        sb.append(keyChar);
        selectNextFileStarting(sb.toString());
        lastTimeTyped = System.currentTimeMillis();
      }

    }
  }

  private final class BaseNavigateActionGoUp extends BaseNavigateAction {
    private BaseNavigateActionGoUp(VfsBrowser browser) {
      super(browser);
      putValue(SMALL_ICON, Icons.getInstance().getArrowTurn90());
      putValue(SHORT_DESCRIPTION, Messages.getMessage("nav.goFolderUp"));
    }

    @Override
    public void performLongOperation(CheckBeforeActionResult actionResult) {
      LOGGER.info("Executing going up");
      try {
        goToFileObject(currentLocation.getParent());
      } catch (FileSystemException e) {
        LOGGER.error("Error go UP", e);
      }
    }

    @Override
    protected boolean canGoUrl() {
      try {
        FileObject parent = currentLocation.getParent();
        return parent != null && VFSUtils.canGoUrl(parent);
      } catch (FileSystemException e) {
        LOGGER.error("Can't get parent of current location", e);
      }
      return false;
    }

    @Override
    protected boolean canExecuteDefaultAction() {
      return false;
    }
  }

  private final class BaseNavigateActionOpen extends BaseNavigateAction {
    private BaseNavigateActionOpen(VfsBrowser browser) {
      super(browser);
    }

    @Override
    public void performLongOperation(CheckBeforeActionResult checkBeforeActionResult) {
      int selectedRow = tableFiles.getSelectedRow();
      FileObject fileObject = vfsTableModel.get(tableFiles.convertRowIndexToModel(selectedRow));
      if (canExecuteDefaultAction() && actionApproveButton.isEnabled()) {
        SwingUtilities.invokeLater(actionApproveButton::doClick);
        cancelScheduledLoading();
      } else {
        goToFileObject(fileObject);
      }
    }

    @Override
    protected boolean canGoUrl() {
      int selectedRow = tableFiles.getSelectedRow();
      if (selectedRow > -1) {
        FileObject fileObject = vfsTableModel.get(tableFiles.convertRowIndexToModel(selectedRow));
        return VFSUtils.canGoUrl(fileObject);
      }
      return false;

    }

    @Override
    protected boolean canExecuteDefaultAction() {
      int selectedRow = tableFiles.getSelectedRow();
      if (SelectionMode.FILES_ONLY.equals(selectionMode) || SelectionMode.DIRS_AND_FILES.equals(selectionMode)) {
        if (selectedRow > -1) {
          FileObject fileObject = vfsTableModel.get(tableFiles.convertRowIndexToModel(selectedRow));
          try {
            return FileType.FILE.equals(fileObject.getType()) || FileType.FILE_OR_FOLDER.equals(fileObject.getType());
          } catch (FileSystemException e) {
            LOGGER.warn("Cant' get file type", e);
          }
        }
      }
      return false;
    }
  }

  private final class BaseNavigateActionRefresh extends BaseNavigateAction {
    private BaseNavigateActionRefresh(VfsBrowser browser) {
      super(browser);
      putValue(SMALL_ICON, Icons.getInstance().getArrowCircleDouble());
      putValue(SHORT_DESCRIPTION, Messages.getMessage("nav.refreshActionLabelText"));
    }

    @Override
    public void performLongOperation(CheckBeforeActionResult checkBeforeActionResult) {
      try {
        currentLocation.refresh();
      } catch (FileSystemException e) {
        LOGGER.error("Can't refresh location", e);
      }
      goToFileObject(currentLocation);
    }

    @Override
    protected boolean canGoUrl() {
      return true;
    }

    @Override
    protected boolean canExecuteDefaultAction() {
      return false;
    }
  }


}
