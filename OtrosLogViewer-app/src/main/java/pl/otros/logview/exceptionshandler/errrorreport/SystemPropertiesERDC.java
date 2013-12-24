package pl.otros.logview.exceptionshandler.errrorreport;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SystemPropertiesERDC implements ErrorReportDataCollector {

	@Override
	public Map<String, String> collect(ErrorReportCollectingContext context) {
		Map<String, String> r = new HashMap<String, String>();
		Properties properties = System.getProperties();
		fillValues(r, properties);
		return r ;
	}
	
	protected void fillValues(Map<String, String> map,Properties properties){
		for (Object keyObject : properties.keySet()) {
			String key = keyObject.toString();
			String property = properties.getProperty(key);
			if (key.contains("pass")){
				property = "*****";
			}
			map.put("SYSTEM_PROPERTIES:"+key, property);
		}
		
	}

}
