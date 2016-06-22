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
package pl.otros.logview.api;

import org.apache.commons.configuration.DataConfiguration;
import org.jdesktop.swingx.JXTable;
import pl.otros.logview.api.gui.*;
import pl.otros.logview.api.loading.LogLoader;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.api.plugins.MenuActionProvider;
import pl.otros.logview.api.services.Services;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
  private final List<MenuActionProvider> menuActionProviders;
  private LogLoader logLoader;
  private Services services;

  public OtrosApplication() {
    menuActionProviders = new ArrayList<>();
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

  public Optional<LogViewPanelI> getSelectedLogViewPanel() {
    int selectedIndex = jTabbedPane.getSelectedIndex();
    if (selectedIndex>0){
      Component componentAt = jTabbedPane.getComponentAt(selectedIndex);
      if (componentAt instanceof LogViewPanelWrapper) {
        LogViewPanelWrapper logViewPanelWrapper = (LogViewPanelWrapper) componentAt;
        return Optional.of(logViewPanelWrapper.getLogViewPanel());
      } else if (componentAt instanceof LogPatternParserEditor) {
        LogPatternParserEditor patternParserEditor = (LogPatternParserEditor) componentAt;
        return Optional.of(patternParserEditor.getLogViewPanel());
      }
    }
    return Optional.empty();
  }

  public Optional<LogDataTableModel> getSelectedPaneLogDataTableModel() {
    Optional<LogViewPanelI> selectedLogViewPanel = getSelectedLogViewPanel();
    return selectedLogViewPanel.map(LogViewPanelI::getDataTableModel);
  }

  public Optional<JXTable> getSelectPaneJXTable() {
    Optional<LogViewPanelI> selectedLogViewPanel = getSelectedLogViewPanel();
    return selectedLogViewPanel.map(LogViewPanelI::getTable);
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

  public void setSearchField(JTextField searchField) {
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

  public LogLoader getLogLoader() {
    return logLoader;
  }

  public void setLogLoader(LogLoader logLoader) {
    this.logLoader = logLoader;
  }

  public void addLogViewPanelMenuActionProvider(MenuActionProvider menuActionProviders) {
    this.menuActionProviders.add(menuActionProviders);
  }

  /**
   * Return menu action providers
   *
   * @return
   */
  public List<MenuActionProvider> getLogViewPanelMenuActionProvider() {
    return new ArrayList<>(menuActionProviders);
  }

}

