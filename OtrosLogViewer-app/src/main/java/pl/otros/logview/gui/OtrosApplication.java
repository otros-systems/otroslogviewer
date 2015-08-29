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

import org.apache.commons.configuration.DataConfiguration;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.gui.services.persist.PersistService;
import pl.otros.logview.api.plugins.MenuActionProvider;
import pl.otros.logview.gui.services.Services;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Krzysztof Otrebski
 * Date: 3/29/12
 * Time: 6:57 AM
 */
public class OtrosApplication {
  private DataConfiguration configuration = null;
  private AllPluginables allPluginables;
  private StatusObserver statusObserver;
  private JFrame applicationJFrame;
  private JTabbedPane jTabbedPane;
  private JTextField searchField;
  private MarkerColors selectedMarkColors;
  private JMenu pluginsMenu;
  private JOtrosVfsBrowserDialog otrosVfsBrowserDialog;
  private AppProperties appProperties;
  private List<MenuActionProvider> menuActionProviders;
  private PersistService persistService;
  private final List<MenuActionProvider> menuActionProviders;


  private Services services;

  public OtrosApplication(){
    menuActionProviders=new ArrayList<MenuActionProvider>();
    appProperties = new AppProperties();
  }

  public AppProperties getAppProperties() {
    return appProperties;
  }

  public void setAppProperties(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  public JTabbedPane getjTabbedPane() {
    return jTabbedPane;
  }

  public JOtrosVfsBrowserDialog getOtrosVfsBrowserDialog() {
    return otrosVfsBrowserDialog;
  }

  public void setOtrosVfsBrowserDialog(JOtrosVfsBrowserDialog otrosVfsBrowserDialog) {
    this.otrosVfsBrowserDialog = otrosVfsBrowserDialog;
  }

  public JMenu getPluginsMenu() {
    return pluginsMenu;
  }

  public void setPluginsMenu(JMenu pluginsMenu) {
    this.pluginsMenu = pluginsMenu;
  }

  public JTextField getSearchField() {
    return searchField;
  }

  public JTabbedPane getJTabbedPane() {
    return jTabbedPane;
  }

  public LogViewPanel getSelectedLogViewPanel() {
    int selectedIndex = jTabbedPane.getSelectedIndex();
    Component componentAt = jTabbedPane.getComponentAt(selectedIndex);
    if (componentAt instanceof LogViewPanelWrapper) {
      LogViewPanelWrapper logViewPanelWrapper = (LogViewPanelWrapper) componentAt;
      return logViewPanelWrapper.getLogViewPanel();
    } else if (componentAt instanceof Log4jPatternParserEditor) {
      Log4jPatternParserEditor patternParserEditor = (Log4jPatternParserEditor) componentAt;
      LogViewPanel logViewPanel = patternParserEditor.getLogViewPanel();
      return logViewPanel;
    }
    return null;
  }

  public LogDataTableModel getSelectedPaneLogDataTableModel() {
    LogViewPanel selectedLogViewPanel = getSelectedLogViewPanel();
    if (selectedLogViewPanel != null) {
      return selectedLogViewPanel.getDataTableModel();
    }
    return null;
  }

  public JXTable getSelectPaneJXTable() {
    LogViewPanel selectedLogViewPanel = getSelectedLogViewPanel();
    if (selectedLogViewPanel != null) {
      return selectedLogViewPanel.getTable();
    }
    return null;
  }

  public void addClosableTab(String name, String tooltip, Icon icon, JComponent component, boolean show) {
    JTabbedPane tabbedPane = getJTabbedPane();
    if (tabbedPane.indexOfComponent(component) == -1) {
      int tabCount = tabbedPane.getTabCount();
      tabbedPane.addTab(name, icon, component);
      tabbedPane.setTabComponentAt(tabCount, new TabHeader(tabbedPane, name, icon, tooltip));
      tabbedPane.setSelectedIndex(tabCount);
    }
    if (show) {
      tabbedPane.setSelectedComponent(component);
    }
  }

  public JFrame getApplicationJFrame() {
    return applicationJFrame;
  }

  public DataConfiguration getConfiguration() {
    return configuration;
  }

  public AllPluginables getAllPluginables() {
    return allPluginables;
  }

  public StatusObserver getStatusObserver() {
    return statusObserver;
  }

  public void setAllPluginables(AllPluginables allPluginables) {
    this.allPluginables = allPluginables;
  }

  public void setApplicationJFrame(JFrame applicationJFrame) {
    this.applicationJFrame = applicationJFrame;
  }

  public void setConfiguration(DataConfiguration configuration) {
    this.configuration = configuration;
  }

  public void setjTabbedPane(JTabbedPane jTabbedPane) {
    this.jTabbedPane = jTabbedPane;
  }

  public void setSearchField(JXComboBox searchField) {
    this.searchField = searchField;
  }

  public void setStatusObserver(StatusObserver statusObserver) {
    this.statusObserver = statusObserver;
  }

  public MarkerColors getSelectedMarkColors() {
    return selectedMarkColors;
  }

  public void setSelectedMarkColors(MarkerColors selectedMarkColors) {
    this.selectedMarkColors = selectedMarkColors;
  }

  public Services getServices() {
    return services;
  }

  public void setServices(Services services) {
    this.services = services;
  }

  public void addLogViewPanelMenuActionProvider(MenuActionProvider menuActionProviders) {
    this.menuActionProviders.add(menuActionProviders);
  }

  /**
   * Return menu action providers
   * @return
   */
  public List<MenuActionProvider> getLogViewPanelMenuActionProvider(){
    return new ArrayList<MenuActionProvider>(menuActionProviders);
  }

  public PersistService getPersistService() {
    return persistService;
  }

  public void setPersistService(PersistService persistService) {
    this.persistService = persistService;
  }
}

