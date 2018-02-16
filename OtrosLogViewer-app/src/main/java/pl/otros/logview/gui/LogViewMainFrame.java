/*
 * Copyright 2013 Krzysztof Otrebski
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

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.negusoft.singleinstance.SingleInstance;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.VersionUtil;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.Ide;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelI;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.pluginable.*;
import pl.otros.logview.api.plugins.Plugin;
import pl.otros.logview.batch.BatchProcessor;
import pl.otros.logview.exceptionshandler.*;
import pl.otros.logview.filter.QueryFilter;
import pl.otros.logview.gui.actions.*;
import pl.otros.logview.gui.actions.JumpToMarkedAction.Direction;
import pl.otros.logview.gui.actions.globalhotkeys.FocusComponentOnHotKey;
import pl.otros.logview.gui.actions.globalhotkeys.KeyboardTabSwitcher;
import pl.otros.logview.gui.actions.read.DragAndDropFilesHandler;
import pl.otros.logview.gui.actions.search.*;
import pl.otros.logview.gui.actions.search.SearchAction.SearchMode;
import pl.otros.logview.gui.browser.LogParsableListener;
import pl.otros.logview.gui.editor.json.JsonPatternParserEditor;
import pl.otros.logview.gui.editor.log4j.Log4jPatternParserEditor;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.gui.message.SoapMessageFormatter;
import pl.otros.logview.gui.message.update.MessageUpdateUtils;
import pl.otros.logview.gui.renderers.MarkerColorsComboBoxRenderer;
import pl.otros.logview.gui.services.jumptocode.ServicesImpl;
import pl.otros.logview.gui.suggestion.PersistedSuggestionSource;
import pl.otros.logview.gui.suggestion.SearchSuggestionRenderer;
import pl.otros.logview.gui.suggestion.SearchSuggestionSource;
import pl.otros.logview.gui.tip.TipOfTheDay;
import pl.otros.logview.gui.util.DelayedSwingInvoke;
import pl.otros.logview.gui.util.DocumentInsertUpdateHandler;
import pl.otros.logview.ide.IdeAvailabilityCheck;
import pl.otros.logview.ide.IdeIntegrationConfigAction;
import pl.otros.logview.loader.IconsLoader;
import pl.otros.logview.loader.LvDynamicLoader;
import pl.otros.logview.logloader.basic.BasicLogLoader;
import pl.otros.logview.pluginsimpl.PluginContextImpl;
import pl.otros.logview.reader.SocketLogReader;
import pl.otros.logview.singleinstance.SingleInstanceRequestResponseDelegate;
import pl.otros.swing.config.OtrosConfiguration;
import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;
import pl.otros.swing.rulerbar.RulerBarHelper;
import pl.otros.swing.suggest.SuggestDecorator;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static pl.otros.logview.api.ConfKeys.*;

public class LogViewMainFrame extends JFrame {
  public static final String VFS_IDENTITIES = "vfs.Identities";
  private static final Logger LOGGER = LoggerFactory.getLogger(LogViewMainFrame.class.getName());
  private static final String CARD_LAYOUT_LOGS_TABLE = "cardLayoutLogsTable";
  private static final String CARD_LAYOUT_EMPTY = "cardLayoutEmpty";
  public static final String RUN_FOR_SCENARIO_TEST = "runForScenarioTest";
  private JToolBar toolBar;
  private JLabelStatusObserver observer;
  private JTabbedPane logsTabbedPane;
  private EnableDisableComponetsForTabs enableDisableComponetsForTabs;
  private DataConfiguration configuration;
  private CardLayout cardLayout;
  private JPanel cardLayoutPanel;
  private JTextField searchField;
  private AllPluginables allPluginables;
  private PluginableElementsContainer<LogImporter> logImportersContainer;
  private SearchResultColorizer searchResultColorizer;
  private OtrosApplication otrosApplication;
  private ExitAction exitAction;
  private java.util.List<SocketLogReader> logReaders = new ArrayList<>();

  public LogViewMainFrame(DataConfiguration c) throws InitializationException {
    super();
    this.configuration = c;
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    String title = "OtrosLogViewer";
    try {
      title += ' ' + new VersionUtil().getRunningVersion();
    } catch (Exception e) {
      LOGGER.warn("Can't load version of running OLV");
    }
    this.setTitle(title);
    try {
      String iconPath = "img/otros/logo16.png";
      if (System.getProperty("os.name").contains("Linux")) {
        iconPath = "img/otros/logo64.png";
      }
      BufferedImage icon = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(iconPath));
      this.setIconImage(icon);
    } catch (Exception e1) {
      LOGGER.warn("Can't load icon: " + e1.getMessage());
    }
    Exception modalDisplayException = null;
    // If non-terminal load problem occurs, queue to display for user on
    // top of the app UI.
    try {
      OtrosSplash.setMessage("Loading plugins");
      LvDynamicLoader.getInstance().setStatusObserver(OtrosSplash.getSplashStatusObserver());
      LvDynamicLoader.getInstance().loadAll();
      OtrosSplash.setMessage("Loading plugins loaded");
    } catch (IOException e) {
      LOGGER.error("Problem with loading automatic markers, filter or log importers: " + e.getMessage());
      modalDisplayException = e;
    } catch (InitializationException ie) {
      // Details should have been logged at lower level
      modalDisplayException = ie;
    }
    OtrosSplash.setMessage("Initializing GUI");
    allPluginables = AllPluginables.getInstance();
    logImportersContainer = allPluginables.getLogImportersContainer();
    PluginableElementsContainer<MessageColorizer> messageColorizercontainer = allPluginables.getMessageColorizers();
    searchResultColorizer = (SearchResultColorizer) messageColorizercontainer.getElement(SearchResultColorizer.class.getName());
    cardLayout = new CardLayout();
    cardLayoutPanel = new JPanel(cardLayout);
    JLabel statusLabel = new JLabel(" ");
    observer = new JLabelStatusObserver(statusLabel);
    logsTabbedPane = new JTabbedPane();
    logsTabbedPane.setName("MainFrame.tabbedPane");
    enableDisableComponetsForTabs = new EnableDisableComponetsForTabs(logsTabbedPane);
    logsTabbedPane.addChangeListener(enableDisableComponetsForTabs);

    otrosApplication = new OtrosApplication();
    otrosApplication.setAllPluginables(AllPluginables.getInstance());
    otrosApplication.setApplicationJFrame(this);
    otrosApplication.setConfiguration(configuration);
    otrosApplication.setjTabbedPane(logsTabbedPane);
    otrosApplication.setStatusObserver(observer);
    final LogParsableListener logParsableListener = new LogParsableListener(otrosApplication.getAllPluginables().getLogImportersContainer());
    otrosApplication.setOtrosVfsBrowserDialog(new JOtrosVfsBrowserDialog(getVfsFavoritesConfiguration(), logParsableListener));
    otrosApplication.setServices(new ServicesImpl(otrosApplication));
    otrosApplication.setLogLoader(new BasicLogLoader());
    if (!runningForTests()){
      SingleInstanceRequestResponseDelegate.getInstance().setOtrosApplication(otrosApplication);
    }
    ToolTipManager.sharedInstance().setDismissDelay(5000);

    JProgressBar heapBar = new JProgressBar();
    heapBar.setPreferredSize(new Dimension(190, 15));
    final Thread memoryUsedUpdater = new Thread(new MemoryUsedStatsUpdater(heapBar, 1500), "MemoryUsedUpdater");
    memoryUsedUpdater.setDaemon(true);
    memoryUsedUpdater.start();
    JPanel statusPanel = new JPanel(new MigLayout("fill", "[fill, push, grow][right][right]", "[]"));
    statusPanel.add(statusLabel);
    final JButton ideConnectedLabel = new JButton(Ide.IDEA.getIconDiscounted());
    statusPanel.add(ideConnectedLabel);
    statusPanel.add(new JButton(new SwitchAutoJump(otrosApplication)));
    statusPanel.add(heapBar);

    initMenu();
    initToolbar();
    addEmptyViewListener();
    addMenuBarReloadListener();
    otrosApplication.setSearchField(searchField);
    cardLayoutPanel.add(logsTabbedPane, CARD_LAYOUT_LOGS_TABLE);
    EmptyViewPanel emptyViewPanel = new EmptyViewPanel(otrosApplication, logReaders );
    final JScrollPane jScrollPane = new JScrollPane(emptyViewPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    SwingUtilities.invokeLater(() -> jScrollPane.getVerticalScrollBar().setValue(0));
    cardLayoutPanel.add(jScrollPane, CARD_LAYOUT_EMPTY);
    cardLayout.show(cardLayoutPanel, CARD_LAYOUT_EMPTY);
    enableDisableComponetsForTabs.stateChanged(null);
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(toolBar, BorderLayout.NORTH);
    cp.add(cardLayoutPanel, BorderLayout.CENTER);
    cp.add(statusPanel, BorderLayout.SOUTH);
    initGlobalHotKeys();
    initInputMap();
    initPosition();
    if (configuration.getBoolean(ConfKeys.LOAD_EXPERIMENTAL_FEATURES, false)) {
      initExperimental();
    }
    setTransferHandler(new DragAndDropFilesHandler(otrosApplication));
    initPlugins();
    OtrosSplash.hide();
    setVisible(true);
    if (modalDisplayException != null)
      JOptionPane.showMessageDialog(this,
        "Problem with loading automatic markers,"
          + "filter or log importers:\n"
          + modalDisplayException.getMessage(), "Initialization Error",
        JOptionPane.ERROR_MESSAGE);

    if (!runningForTests()) {
      new TipOfTheDay(c).showTipOfTheDayIfNotDisabled(this);
      Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueueProxy());
      // Check new version on start
      if (c.getBoolean(ConfKeys.VERSION_CHECK_ON_STARTUP, true)) {
        new ChekForNewVersionOnStartupAction(otrosApplication).actionPerformed(null);
      }
    }
    ListUncaughtExceptionHandlers listUncaughtExceptionHandlers = new ListUncaughtExceptionHandlers(//
      new LoggingExceptionHandler(),//
      new ShowErrorDialogExceptionHandler(otrosApplication),//
      new StatusObserverExceptionHandler(observer)
    );
    Thread.setDefaultUncaughtExceptionHandler(listUncaughtExceptionHandlers);
    ListeningScheduledExecutorService listeningScheduledExecutorService = otrosApplication.getServices().getTaskSchedulerService().getListeningScheduledExecutorService();
    listeningScheduledExecutorService.scheduleAtFixedRate(
      new IdeAvailabilityCheck(ideConnectedLabel, otrosApplication.getServices().getJumpToCodeService()),
      25, 25, TimeUnit.SECONDS);
    ideConnectedLabel.addActionListener(new IdeIntegrationConfigAction(otrosApplication));
  }

  private static boolean runningForTests() {
    final String runForTest = System.getProperty(RUN_FOR_SCENARIO_TEST, "false");
    final boolean b = runForTest.equals("true");
    return b;
  }

  private void initInputMap() {
    final JComponent contentPane = (JComponent) this.getContentPane();
    final InputMap inputMapInFocusedWindow = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    String parseClipboard = "parseClipboard";
    final JTextArea jTextArea = new JTextArea();
    final KeyStroke[] keyStrokes = jTextArea.getInputMap().allKeys();
    Arrays.asList(keyStrokes).forEach(ks -> LOGGER.debug("LogViewMainFrame.initInputMap: " + ks.toString() + " -> " + jTextArea.getInputMap().get(ks)));

    inputMapInFocusedWindow.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), parseClipboard);
    inputMapInFocusedWindow.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.SHIFT_MASK), parseClipboard);
    contentPane.getActionMap().put(parseClipboard, new ParseClipboard(otrosApplication));
  }

  /**
   * @param args porgram CLI arguments
   * @throws InitializationException
   * @throws InvocationTargetException
   * @throws InterruptedException
   */
  public static void main(final String[] args) throws InitializationException, InterruptedException, InvocationTargetException {
    if (args.length > 0 && "-batch".equals(args[0])) {
      try {
        String[] batchArgs = new String[args.length - 1];
        System.arraycopy(args, 1, batchArgs, 0, batchArgs.length);
        BatchProcessor.main(batchArgs);
      } catch (IOException | ConfigurationException e) {
        System.err.println("Error during batch processing: " + e.getMessage());
        e.printStackTrace();
      }
      return;
    }
    if (!runningForTests()){
      SingleInstanceRequestResponseDelegate singleInstanceRequestResponseDelegate = SingleInstanceRequestResponseDelegate.getInstance();
      SingleInstance singleInstance = SingleInstance.request("OtrosLogViewer", singleInstanceRequestResponseDelegate,
        singleInstanceRequestResponseDelegate, args);
      if (singleInstance == null) {
        LOGGER.info("OtrosLogViewer is already running, params send using requestAction");
        System.exit(0);
      }
    }

    LOGGER.info("Starting application");
    OtrosSplash.setMessage("Starting application");
    OtrosSplash.setMessage("Loading configuration");
    final XMLConfiguration c = getConfiguration("config.xml");
    if (!c.containsKey(ConfKeys.UUID)) {
      c.setProperty(ConfKeys.UUID, java.util.UUID.randomUUID().toString());
    }
    IconsLoader.loadIcons();
    OtrosSplash.setMessage("Loading icons");
    SwingUtilities.invokeAndWait(() -> {
      if (c.getBoolean(APPEARANCE_CUSTOM_FONT_SIZE, false)) {
        final int fontSize = c.getInt(APPEARANCE_FONT_SIZE, 12);
        FontSize.setDefaultSize(fontSize);
      }

      try {
        String lookAndFeel = c.getString(ConfKeys.APPEARANCE_LOOK_AND_FEEL, "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        OtrosSplash.setMessage("Loading L&F " + lookAndFeel);
        LOGGER.debug("Initializing look and feel: " + lookAndFeel);
        PlasticLookAndFeel.setTabStyle(Plastic3DLookAndFeel.TAB_STYLE_METAL_VALUE);
        UIManager.setLookAndFeel(lookAndFeel);
      } catch (Throwable e1) {
        LOGGER.warn("Cannot initialize LookAndFeel: " + e1.getMessage());
      }
      try {
        final DataConfiguration c1 = new OtrosConfiguration(c);
        final LogViewMainFrame mf = new LogViewMainFrame(c1);
        mf.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            c.setProperty("gui.state", mf.getExtendedState());
            if (mf.getExtendedState() == Frame.NORMAL) {
              c.setProperty("gui.width", mf.getWidth());
              c.setProperty("gui.height", mf.getHeight());
            }
          }

          @Override
          public void componentMoved(ComponentEvent e) {
            c.setProperty("gui.location.x", mf.getLocation().x);
            c.setProperty("gui.location.y", mf.getLocation().y);
          }
        });
        mf.addWindowListener(mf.exitAction);
        SingleInstanceRequestResponseDelegate.openFilesFromStartArgs(mf.otrosApplication, Arrays.asList(args),
          mf.otrosApplication.getAppProperties().getCurrentDir());
      } catch (InitializationException e) {
        LOGGER.error("Cannot initialize main frame", e);
      }
    });
  }

  private static XMLConfiguration getConfiguration(String file) {
    XMLConfiguration commonConfiguration = new XMLConfiguration();
    File commonConfigurationFile = new File(file);
    // load common configuration
    if (commonConfigurationFile.exists()) {
      LOGGER.info("Loading common configuration from " + commonConfigurationFile.getAbsolutePath());
      try {
        commonConfiguration.load(commonConfigurationFile);
      } catch (ConfigurationException e) {
        LOGGER.error("Can't load configuration, creating new " + e.getMessage());
      }
    } else {
      LOGGER.info("Common configuration file do not exist");
    }
    // load user specific configuration
    if (!AllPluginables.USER_CONFIGURATION_DIRECTORY.exists()) {
      LOGGER.info("Creating user specific OtrosLogViewer configuration directory " + AllPluginables.USER_CONFIGURATION_DIRECTORY.getAbsolutePath());
      AllPluginables.USER_CONFIGURATION_DIRECTORY.mkdirs();
      AllPluginables.USER_FILTER.mkdirs();
      AllPluginables.USER_LOG_IMPORTERS.mkdirs();
      AllPluginables.USER_MARKERS.mkdirs();
      AllPluginables.USER_MESSAGE_FORMATTER_COLORZIERS.mkdirs();
    }
    XMLConfiguration userConfiguration = new XMLConfiguration();
    File userConfigurationFile = new File(AllPluginables.USER_CONFIGURATION_DIRECTORY + File.separator + file);
    userConfiguration.setFile(userConfigurationFile);
    if (userConfigurationFile.exists()) {
      try {
        userConfiguration.load();
      } catch (ConfigurationException e) {
        LOGGER.error(String.format("Can't load user configuration from %s: %s", userConfigurationFile.getAbsolutePath(), e.getMessage()));
      }
    }
    Iterator<?> keys = commonConfiguration.getKeys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      if (!userConfiguration.containsKey(key)) {
        userConfiguration.setProperty(key, commonConfiguration.getProperty(key));
      }
    }
    //set default chunks order if not found in common or user config
    if (!userConfiguration.containsKey(ConfKeys.MESSAGE_FORMATTER_CHUNKS_ORDER)) {
      userConfiguration.setProperty(ConfKeys.MESSAGE_FORMATTER_CHUNKS_ORDER,
        "date;class;method;level;thread;file;NDC;logger;properties;message;marked;note");
    }
    //default setting to use monospace in message chunk or not
    if (!userConfiguration.containsKey(ConfKeys.MESSAGE_FORMATTER_USE_MONOSPACE_FONT_IN_MESSAGE_CHUNK)) {
      userConfiguration.setProperty(ConfKeys.MESSAGE_FORMATTER_USE_MONOSPACE_FONT_IN_MESSAGE_CHUNK,
        false);
    }
    userConfiguration.setAutoSave(true);
    return userConfiguration;
  }

  private static Configuration getVfsFavoritesConfiguration() {
    File file = new File(AllPluginables.USER_CONFIGURATION_DIRECTORY + File.separator + "vfsFavorites.xml");
    XMLConfiguration configuration = new XMLConfiguration();
    configuration.setFile(file);
    if (file.exists()) {
      try {
        configuration.load();
      } catch (ConfigurationException e) {
        LOGGER.error(String.format("Can't load user configuration from %s: %s", file.getAbsolutePath(), e.getMessage()));
      }
    }
    configuration.setAutoSave(true);
    return configuration;
  }

  private void initPlugins() {
    LOGGER.info("Loading plugins");
    PluginContextImpl contextImpl = new PluginContextImpl(otrosApplication);
    PluginableElementsContainer<PluginablePluginAdapter> pluginsInfoContainer = AllPluginables.getInstance().getPluginsInfoContainer();
    for (PluginablePluginAdapter ppi : pluginsInfoContainer.getElements()) {
      try {
        Plugin plugin = ppi.getPlugin();
        plugin.initialize(contextImpl);
      } catch (Exception e) {
        LOGGER.error("Can't initialize plugins " + ppi.getName() + "\n" + Throwables.getStackTraceAsString(e));
      }
    }
  }

  private void addEmptyViewListener() {
    logsTabbedPane.addContainerListener(new ContainerListener() {
      @Override
      public void componentRemoved(ContainerEvent e) {
        setCard();
      }

      @Override
      public void componentAdded(ContainerEvent e) {
        setCard();
      }

      public void setCard() {
        int tabCount = logsTabbedPane.getTabCount();
        if (tabCount > 0) {
          cardLayout.show(cardLayoutPanel, CARD_LAYOUT_LOGS_TABLE);
        } else {
          cardLayout.show(cardLayoutPanel, CARD_LAYOUT_EMPTY);
        }
      }
    });
  }

  private void addMenuBarReloadListener() {
    PluginableElementEventListener<LogImporter> l = new PluginableElementEventListener<LogImporter>() {
      @Override
      public void elementAdded(LogImporter element) {
        reloadMenuBar();
      }

      @Override
      public void elementRemoved(LogImporter element) {
        reloadMenuBar();
      }

      @Override
      public void elementChanged(LogImporter element) {
        reloadMenuBar();
      }

      private void reloadMenuBar() {
        GuiUtils.runLaterInEdt(() -> {
          initMenu();
          //without validating tree menu bar is inactive
          synchronized (LogViewMainFrame.this.getTreeLock()) {
            LogViewMainFrame.this.validateTree();
          }
        });
      }
    };
    otrosApplication.getAllPluginables().getLogImportersContainer().addListener(l);
  }

  private void initToolbar() {
    toolBar = new JToolBar(){

    };
    final JComboBox searchMode = new JComboBox(new String[]{"String contains search: ", "Regex search: ", "Query search: "});
    searchMode.setName("MainFrame.searchMode");
    searchField = new JTextField();
    searchField.setName("MainFrame.searchField");
    PersistedSuggestionSource searchSuggestionSource = new PersistedSuggestionSource(new SearchSuggestionSource(SearchMode.STRING_CONTAINS), otrosApplication.getServices().getPersistService());
    SuggestDecorator.decorate(
      searchField,
      searchSuggestionSource,
      new SearchSuggestionRenderer(),
      s -> searchField.setText(s.getValue().getFullContent()));
    searchField.setEditable(true);

    final SearchListener searchListener = searchSuggestionSource::addHistory;
    final SearchAction searchActionForward = new SearchAction(otrosApplication, SearchDirection.FORWARD, searchListener);
    final SearchAction searchActionBackward = new SearchAction(otrosApplication, SearchDirection.REVERSE, searchListener);
    searchField.setMinimumSize(new Dimension(150, 10));
    searchField.setPreferredSize(new Dimension(250, 10));
    searchField.setToolTipText("<HTML>Enter text to search.<BR/>" + "Enter - search next,<BR/>Alt+Enter search previous,<BR/>"
      + "Ctrl+Enter - mark all found</HTML>");
    final DelayedSwingInvoke delayedSearchResultUpdate = new DelayedSwingInvoke() {
      @Override
      protected void performActionHook() {
        int stringEnd = searchField.getSelectionStart();
        if (stringEnd < 0) {
          stringEnd = searchField.getText().length();
        }
        try {
          String selectedText = searchField.getText(0, stringEnd);
          if (StringUtils.isBlank(selectedText)) {
            return;
          }
          final Optional<LogViewPanelI> selectedLogViewPanel = otrosApplication.getSelectedLogViewPanel();
          selectedLogViewPanel.ifPresent(selectedPanel -> {
            OtrosJTextWithRulerScrollPane<JTextPane> logDetailWithRulerScrollPane = selectedPanel.getLogDetailWithRulerScrollPane();
            MessageUpdateUtils.highlightSearchResult(logDetailWithRulerScrollPane, otrosApplication.getAllPluginables().getMessageColorizers());
            RulerBarHelper.scrollToFirstMarker(logDetailWithRulerScrollPane);
          });
        } catch (BadLocationException e) {
          LOGGER.error("Can't update search highlight", e);
        }
      }
    };
    searchField.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
      @Override
      protected void documentChanged(DocumentEvent e) {
        delayedSearchResultUpdate.performAction();
      }
    });
    final MarkAllFoundAction markAllFoundAction = new MarkAllFoundAction(otrosApplication);
    final SearchModeValidatorDocumentListener searchValidatorDocumentListener = new SearchModeValidatorDocumentListener(searchField, observer,
      SearchMode.STRING_CONTAINS);
    SearchMode searchModeFromConfig = configuration.get(SearchMode.class, "gui.searchMode", SearchMode.STRING_CONTAINS);
    final String lastSearchString;
    int selectedSearchMode = 0;
    if (searchModeFromConfig.equals(SearchMode.STRING_CONTAINS)) {
      selectedSearchMode = 0;
      lastSearchString = configuration.getString(ConfKeys.SEARCH_LAST_STRING, "");
    } else if (searchModeFromConfig.equals(SearchMode.REGEX)) {
      selectedSearchMode = 1;
      lastSearchString = configuration.getString(ConfKeys.SEARCH_LAST_REGEX, "");
    } else if (searchModeFromConfig.equals(SearchMode.QUERY)) {
      selectedSearchMode = 2;
      lastSearchString = configuration.getString(ConfKeys.SEARCH_LAST_QUERY, "");
    } else {
      LOGGER.warn("Unknown search mode " + searchModeFromConfig);
      lastSearchString = "";
    }
    final JTextField sfTf = searchField;
    sfTf.getDocument().addDocumentListener(searchValidatorDocumentListener);
    sfTf.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
      @Override
      protected void documentChanged(DocumentEvent e) {
        try {
          int length = e.getDocument().getLength();
          if (length > 0) {
            searchResultColorizer.setSearchString(e.getDocument().getText(0, length));
          }
        } catch (BadLocationException e1) {
          LOGGER.error("Error: ", e1);
        }
      }
    });
    sfTf.addKeyListener(new SearchFieldKeyListener(searchActionForward, sfTf));
    sfTf.setText(lastSearchString);
    searchMode.addActionListener(e -> {
      SearchMode mode = null;
      boolean validationEnabled = false;
      String lastSearch = searchField.getText();
      if (searchMode.getSelectedIndex() == 0) {
        mode = SearchMode.STRING_CONTAINS;
        searchValidatorDocumentListener.setSearchMode(mode);
        validationEnabled = false;
        searchMode.setToolTipText("Checking if log message contains string (case is ignored)");
      } else if (searchMode.getSelectedIndex() == 1) {
        mode = SearchMode.REGEX;
        validationEnabled = true;
        searchMode.setToolTipText("Checking if log message matches regular expression (case is ignored)");
      } else if (searchMode.getSelectedIndex() == 2) {
        mode = SearchMode.QUERY;
        validationEnabled = true;
        String querySearchTooltip = "<HTML>" + //
          "Advance search using SQL-like quries (i.e. level>=warning && msg~=failed && thread==t1)<BR/>" + //
          "Valid operator for query search is ==, ~=, !=, LIKE, EXISTS, <, <=, >, >=, &&, ||, ! <BR/>" + //
          "See wiki for more info<BR/>" + //
          "</HTML>";
        searchMode.setToolTipText(querySearchTooltip);
      }
      searchValidatorDocumentListener.setSearchMode(mode);
      searchValidatorDocumentListener.setEnable(validationEnabled);
      searchActionForward.setSearchMode(mode);
      searchActionBackward.setSearchMode(mode);
      markAllFoundAction.setSearchMode(mode);
      configuration.setProperty("gui.searchMode", mode);
      searchResultColorizer.setSearchMode(mode);
      searchSuggestionSource.setSearchMode(mode);
      searchField.setText(lastSearch);
    });
    searchMode.setSelectedIndex(selectedSearchMode);
    final JCheckBox markFound = new JCheckBox("Mark search result");
    markFound.setMnemonic(KeyEvent.VK_M);
    markFound.setName("MainFrame.markFound");
    searchField.addKeyListener(markAllFoundAction);
    configuration.addConfigurationListener(markAllFoundAction);
    JButton markAllFoundButton = new JButton(markAllFoundAction);
    final JComboBox markColor = new JComboBox(MarkerColors.values());
    markFound.setSelected(configuration.getBoolean("gui.markFound", true));
    markFound.addChangeListener(e -> {
      boolean selected = markFound.isSelected();
      searchActionForward.setMarkFound(selected);
      searchActionBackward.setMarkFound(selected);
      configuration.setProperty("gui.markFound", markFound.isSelected());
    });
    markColor.setRenderer(new MarkerColorsComboBoxRenderer());
//		markColor.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				MarkerColors markerColors = (MarkerColors) markColor.getSelectedItem();
//				searchActionForward.setMarkerColors(markerColors);
//				searchActionBackward.setMarkerColors(markerColors);
//				markAllFoundAction.setMarkerColors(markerColors);
//				configuration.setProperty("gui.markColor", markColor.getSelectedItem());
//				otrosApplication.setSelectedMarkColors(markerColors);
//			}
//		});
    markColor.addItemListener(e -> {
      MarkerColors markerColors = (MarkerColors) markColor.getSelectedItem();
      searchActionForward.setMarkerColors(markerColors);
      searchActionBackward.setMarkerColors(markerColors);
      markAllFoundAction.setMarkerColors(markerColors);
      configuration.setProperty("gui.markColor", markColor.getSelectedItem());
      otrosApplication.setSelectedMarkColors(markerColors);
    });
    markColor.getModel().setSelectedItem(configuration.get(MarkerColors.class, "gui.markColor", MarkerColors.Aqua));
    JButton buttonSearch = new JButton(searchActionForward);
    buttonSearch.setName("MainFrame.searchNext");
    buttonSearch.setMnemonic(KeyEvent.VK_N);
    JButton buttonSearchPrev = new JButton(searchActionBackward);
    buttonSearchPrev.setName("MainFrame.searchPrevious");
    buttonSearchPrev.setMnemonic(KeyEvent.VK_P);
    enableDisableComponetsForTabs.addComponent(buttonSearch);
    enableDisableComponetsForTabs.addComponent(buttonSearchPrev);
    enableDisableComponetsForTabs.addComponent(searchField);
    enableDisableComponetsForTabs.addComponent(markFound);
    enableDisableComponetsForTabs.addComponent(markAllFoundButton);
    enableDisableComponetsForTabs.addComponent(searchMode);
    enableDisableComponetsForTabs.addComponent(markColor);
    toolBar.add(searchMode);
    toolBar.add(searchField);
    toolBar.add(buttonSearch);
    toolBar.add(buttonSearchPrev);
    toolBar.add(markFound);
    toolBar.add(markAllFoundButton);
    toolBar.add(markColor);
    JButton nextMarked = new JButton(new JumpToMarkedAction(otrosApplication, Direction.FORWARD));
    nextMarked.setToolTipText(nextMarked.getText());
    nextMarked.setText("");
    nextMarked.setMnemonic(KeyEvent.VK_E);
    enableDisableComponetsForTabs.addComponent(nextMarked);
    toolBar.add(nextMarked);
    JButton prevMarked = new JButton(new JumpToMarkedAction(otrosApplication, Direction.BACKWARD));
    prevMarked.setToolTipText(prevMarked.getText());
    prevMarked.setText("");
    prevMarked.setMnemonic(KeyEvent.VK_R);
    enableDisableComponetsForTabs.addComponent(prevMarked);
    toolBar.add(prevMarked);
    enableDisableComponetsForTabs.addComponent(toolBar.add(new SearchByLevel(otrosApplication, 1, Level.INFO)))
      .setName("MainFrame.NextInfo");
    enableDisableComponetsForTabs.addComponent(toolBar.add(new SearchByLevel(otrosApplication, 1, Level.WARNING)))
      .setName("MainFrame.NextWarning");;
    enableDisableComponetsForTabs.addComponent(toolBar.add(new SearchByLevel(otrosApplication, 1, Level.SEVERE)))
      .setName("MainFrame.NextSevere");
    enableDisableComponetsForTabs.addComponent(toolBar.add(new SearchByLevel(otrosApplication, -1, Level.INFO)))
      .setName("MainFrame.PreviousInfo");
    enableDisableComponetsForTabs.addComponent(toolBar.add(new SearchByLevel(otrosApplication, -1, Level.WARNING)))
      .setName("MainFrame.PreviousWarning");
    enableDisableComponetsForTabs.addComponent(toolBar.add(new SearchByLevel(otrosApplication, -1, Level.SEVERE)))
      .setName("MainFrame.PreviousSevere");
  }

  private void initMenu() {
    JMenuBar menuBar = getJMenuBar();
    if (menuBar == null) {
      menuBar = new JMenuBar();
      setJMenuBar(menuBar);
    }
    menuBar.removeAll();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    JLabel labelOpenLog = new JLabel("Open log", Icons.FOLDER_OPEN, SwingConstants.LEFT);
    Font menuGroupFont = labelOpenLog.getFont().deriveFont(13f).deriveFont(Font.BOLD);
    labelOpenLog.setFont(menuGroupFont);
    fileMenu.add(labelOpenLog);
    fileMenu.add(new JMenuItem(new AdvanceOpenAction(otrosApplication)));
    fileMenu.add(new ConnectToSocketHubAppenderAction(otrosApplication));
    fileMenu.add(new JSeparator());
    JLabel labelLogInvestigation = new JLabel("Log investigation", SwingConstants.LEFT);
    labelLogInvestigation.setFont(menuGroupFont);
    fileMenu.add(labelLogInvestigation);
    fileMenu.add(new OpenLogInvestigationAction(otrosApplication));
    JMenuItem saveLogsInvest = new JMenuItem(new SaveLogInvestigationAction(otrosApplication));
    enableDisableComponetsForTabs.addComponent(saveLogsInvest);
    fileMenu.add(saveLogsInvest);
    fileMenu.add(new JSeparator());
    JMenuItem exitMenuItem = new JMenuItem("Exit", 'e');
    exitMenuItem.setIcon(Icons.TURN_OFF);
    exitMenuItem.setAccelerator(KeyStroke.getKeyStroke("control F4"));
    exitAction = new ExitAction(otrosApplication);

    exitMenuItem.addActionListener(exitAction);
    fileMenu.add(new JSeparator());
    fileMenu.add(exitMenuItem);
    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.setMnemonic(KeyEvent.VK_T);
    JMenuItem closeAll = new JMenuItem(new CloseAllTabsAction(otrosApplication));
    enableDisableComponetsForTabs.addComponent(closeAll);

    toolsMenu.add(new JMenuItem(new StartSocketListener(otrosApplication, logReaders)));
    toolsMenu.add(new JMenuItem(new StopAllSocketListeners(otrosApplication, logReaders)));
    toolsMenu.add(new ShowMarkersEditor(otrosApplication));
    toolsMenu.add(new ShowLogPatternParserEditor(otrosApplication, "log4jDefaultPatternParser.txt", "Show Log4j pattern parser editor", "Show Log4j pattern parser editor.", Icons.WRENCH, () -> new Log4jPatternParserEditor(otrosApplication, "log4jDefaultPatternParser.txt")));
    toolsMenu.add(new ShowLogPatternParserEditor(otrosApplication, "jsonDefaultPatternParser.properties", "Show JSON pattern parser editor", "Show JSON pattern parser editor.", Icons.JSON, () -> new JsonPatternParserEditor(otrosApplication, "jsonDefaultPatternParser.properties")));
    toolsMenu.add(new ConvertLogbackLog4jPatternAction(otrosApplication));
    toolsMenu.add(new ShowMessageColorizerEditor(otrosApplication));
    toolsMenu.add(new ShowLoadedPlugins(otrosApplication));
    toolsMenu.add(new ShowOlvLogs(otrosApplication));
    toolsMenu.add(new OpenPreferencesAction(otrosApplication));
    toolsMenu.add(closeAll);
    final ParseClipboard parseClipboard = new ParseClipboard(otrosApplication);
    toolsMenu.add(parseClipboard);
    JMenu pluginsMenu = new JMenu("Plugins");
    otrosApplication.setPluginsMenu(pluginsMenu);
    JMenu helpMenu = new JMenu("Help");
    JMenuItem about = new JMenuItem("About");
    AboutAction action = new AboutAction(otrosApplication);
    action.putValue(Action.NAME, "About");
    about.setAction(action);
    helpMenu.add(about);
    helpMenu.add(new GoToDonatePageAction(otrosApplication));
    JMenuItem checkForNewVersion = new JMenuItem(new CheckForNewVersionAction(otrosApplication));
    helpMenu.add(checkForNewVersion);
    helpMenu.add(new GettingStartedAction(otrosApplication));
    helpMenu.add(new FontSize(otrosApplication, 12));
    menuBar.add(fileMenu);
    menuBar.add(toolsMenu);
    menuBar.add(pluginsMenu);
    menuBar.add(helpMenu);
  }

  private void initExperimental() {
    JMenu menu = new JMenu("Experimental");
    menu.add(new JLabel("Experimental features, can have bugs", Icons.LEVEL_WARNING, SwingConstants.LEADING));
    menu.add(new JSeparator());
    boolean storeOnDisk = StringUtils.equalsIgnoreCase(System.getProperty("cacheEvents"), "true");
    JRadioButtonMenuItem radioButtonMemory = new JRadioButtonMenuItem("Memory - faster, more memory required", !storeOnDisk);
    radioButtonMemory.addActionListener(e -> System.setProperty("cacheEvents", Boolean.FALSE.toString()));
    JRadioButtonMenuItem radioButtonDisk = new JRadioButtonMenuItem("Disk with caching - slower, less memory required", storeOnDisk);
    radioButtonDisk.addActionListener(e -> System.setProperty("cacheEvents", Boolean.TRUE.toString()));
    final ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(radioButtonDisk);
    buttonGroup.add(radioButtonMemory);
    menu.add(new JSeparator(JSeparator.VERTICAL));
    menu.add(new JLabel("Keep parsed log events store:"));
    menu.add(radioButtonMemory);
    menu.add(radioButtonDisk);
    final JCheckBox soapFormatterRemoveMultirefsCbx = new JCheckBox();
    soapFormatterRemoveMultirefsCbx.setSelected(configuration.getBoolean(ConfKeys.FORMATTER_SOAP_REMOVE_MULTIREFS, false));
    AbstractAction enableMultiRefRemoveFeature = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SoapMessageFormatter soapMessageFormatter = (SoapMessageFormatter) AllPluginables.getInstance().getMessageFormatters().getElement(SoapMessageFormatter.class.getName());
        soapMessageFormatter.setRemoveMultiRefs(soapFormatterRemoveMultirefsCbx.isSelected());
        configuration.setProperty(ConfKeys.FORMATTER_SOAP_REMOVE_MULTIREFS, soapFormatterRemoveMultirefsCbx.isSelected());
      }
    };
    enableMultiRefRemoveFeature.putValue(Action.NAME, "Remove mulitRefs from SOAP messages");
    soapFormatterRemoveMultirefsCbx.setAction(enableMultiRefRemoveFeature);
    enableMultiRefRemoveFeature.actionPerformed(null);
    final JCheckBox soapFormatterRemoveXsiForNilElementsCbx = new JCheckBox();
    soapFormatterRemoveXsiForNilElementsCbx.setSelected(configuration.getBoolean(FORMATTER_SOAP_REMOVE_XSI_FOR_NIL, false));
    AbstractAction soapFormatterRemoveXsiFromNilAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SoapMessageFormatter soapMessageFormatter = (SoapMessageFormatter) AllPluginables.getInstance().getMessageFormatters().getElement(SoapMessageFormatter.class.getName());
        soapMessageFormatter.setRemoveXsiForNilElements(soapFormatterRemoveXsiForNilElementsCbx.isSelected());
        configuration.setProperty(FORMATTER_SOAP_REMOVE_XSI_FOR_NIL, soapFormatterRemoveXsiForNilElementsCbx.isSelected());
      }
    };
    soapFormatterRemoveXsiFromNilAction.putValue(Action.NAME, "Remove xsi for for NIL elements from SOAP messages");
    soapFormatterRemoveXsiForNilElementsCbx.setAction(soapFormatterRemoveXsiFromNilAction);
    soapFormatterRemoveXsiFromNilAction.actionPerformed(null);
    menu.add(soapFormatterRemoveMultirefsCbx);
    menu.add(soapFormatterRemoveXsiForNilElementsCbx);
    getJMenuBar().add(menu);
    QueryFilter queryFilter = new QueryFilter();
    allPluginables.getLogFiltersContainer().addElement(queryFilter);
    JButton b = new JButton("Throw exception");
    b.addActionListener(e -> {
      if (System.currentTimeMillis() % 2 == 0) {
        throw new IllegalArgumentException("Exception swing action!");
      } else {
        new Thread(() -> {
          throw new IllegalArgumentException("Exception from tread!");
        }).start();
      }
    });
    menu.add(b);
  }

  private void initGlobalHotKeys() {
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    manager.addKeyEventPostProcessor(new KeyboardTabSwitcher(logsTabbedPane));
    manager.addKeyEventPostProcessor(new FocusComponentOnHotKey(searchField, KeyEvent.VK_F, KeyEvent.META_MASK));
  }

  private void initPosition() {
    Dimension size = new Dimension(configuration.getInt("gui.width", 1280), configuration.getInt("gui.height", 780));
    Point location = new Point(configuration.getInt("gui.location.x", 100), configuration.getInt("gui.location.y", 100));
    int state = configuration.getInt("gui.state", Frame.NORMAL);
    Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
    if (location.x > screensize.width) {
      location.x = 0;
    }
    if (location.y > screensize.height) {
      location.y = 0;
    }
    size.width = Math.min(screensize.width, size.width);
    size.height = Math.min(screensize.height, size.height);
    this.setSize(size);
    this.setLocation(location);
    this.setExtendedState(state);
  }
}
