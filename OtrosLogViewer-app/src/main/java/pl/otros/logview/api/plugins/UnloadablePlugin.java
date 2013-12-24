package pl.otros.logview.api.plugins;

public interface UnloadablePlugin extends Plugin {
	public void unload(PluginContext context);
}
