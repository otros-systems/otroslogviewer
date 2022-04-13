package pl.otros.logview.api.pluginable;

import pl.otros.logview.api.plugins.Plugin;
import pl.otros.logview.api.plugins.PluginInfo;

public class PluginablePluginAdapter implements PluginableElement {

  public PluginInfo pluginInfo;
  private final Plugin plugin;

  public PluginablePluginAdapter(Plugin plugin) {
    super();
    this.plugin = plugin;
    this.pluginInfo = plugin.getPluginInfo();
  }

  @Override
  public String getName() {
    return pluginInfo.getName();
  }

  @Override
  public String getDescription() {
    return pluginInfo.getDescription();
  }

  @Override
  public String getPluginableId() {
    return pluginInfo.getPluginId();
  }

  @Override
  public int getApiVersion() {
    return 0;
  }

  public PluginInfo getPluginInfo() {
    return pluginInfo;
  }

  public Plugin getPlugin() {
    return plugin;
  }


}
