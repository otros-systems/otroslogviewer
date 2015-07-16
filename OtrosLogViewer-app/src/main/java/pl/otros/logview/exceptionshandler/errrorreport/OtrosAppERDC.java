package pl.otros.logview.exceptionshandler.errrorreport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.VersionUtil;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.logview.pluginable.PluginableElement;
import pl.otros.logview.pluginable.PluginableElementsContainer;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OtrosAppERDC implements ErrorReportDataCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtrosAppERDC.class.getName());
    private static final String VERSION = "APPLICATION:version";


    @Override
	public Map<String, String> collect(ErrorReportCollectingContext context) {
		OtrosApplication otrosApplication = context.getOtrosApplication();
		
		HashMap<String, String> r = new HashMap<String, String>();
		r.put("APPLICATION:tab.count", Integer.toString(otrosApplication.getJTabbedPane().getTabCount()));
		AllPluginables allPluginables = otrosApplication.getAllPluginables();
		add("filters",r,allPluginables.getLogFiltersContainer());
		add("logImporters",r,allPluginables.getLogImportersContainer());
		add("markers",r,allPluginables.getMarkersContainser());
		add("messageColorizers",r,allPluginables.getMessageColorizers());
		add("messegeFormatters",r,allPluginables.getMessageFormatters());
		
		RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
		long uptime = rb.getUptime();
		String uptimeFormatted = String.format("%d:%02d:%02d", uptime/3600, (uptime%3600)/60, (uptime%60));
		r.put("APPLICATION:uptime", uptimeFormatted);

        try {
            r.put(VERSION, VersionUtil.getRunningVersion());
        } catch (IOException e) {
            LOGGER.warn("Cannot check running version",e);
        }

        return r;
	}

	private void add(String string, HashMap<String, String> r,
			PluginableElementsContainer<? extends PluginableElement> container) {
		r.put("APPLICATION:plugins."+string+".count",Integer.toString(container.getElements().size()));
		Collection<? extends PluginableElement> elements = container.getElements();
		int id=0;
		for (PluginableElement pluginableElement : elements) {
			r.put("APPLICATION:plugins."+string+"." + id +".id",pluginableElement.getPluginableId());
			r.put("APPLICATION:plugins."+string+"." + id +".name",pluginableElement.getName());
			r.put("APPLICATION:plugins."+string+"." + id +".class",pluginableElement.getClass().getName());
			r.put("APPLICATION:plugins."+string+"." + id +".apiVersion",Integer.toString(pluginableElement.getApiVersion()));			
			id++;
		}
	}

}
