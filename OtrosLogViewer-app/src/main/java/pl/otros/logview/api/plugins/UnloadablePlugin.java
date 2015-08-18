package pl.otros.logview.api.plugins;

public interface UnloadablePlugin extends Plugin {
	void unload(PluginContext context);
}
