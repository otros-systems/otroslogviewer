package pl.otros.logview.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.pluginable.AllPluginables;

public class Test {

	public static void main(String[] args) {
		try {
			LogImportersLoader logImportersLoader;
			logImportersLoader = new LogImportersLoader();
			Collection<LogImporter> logImporters;
			logImporters = new ArrayList<>();
			logImporters.addAll(logImportersLoader.load(AllPluginables.USER_LOG_IMPORTERS));
		    logImporters.addAll(logImportersLoader.loadPropertyPatternFileFromDir(AllPluginables.USER_LOG_IMPORTERS));			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
