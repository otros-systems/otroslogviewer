/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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
 ******************************************************************************/
package pl.otros.logview;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

public class VersionUtil {

	private static final String CURRENT_VERSION_PAGE_URL = "http://otroslogviewer.appspot.com/services/currentVersion?runningVersion=";
	private static final Logger LOGGER = Logger.getLogger(VersionUtil.class.getName());
	public static final String IMPLEMENTATION_VERSION = "Implementation-Version";

	/**
	 * Check latest released version.
	 * @param running
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String getCurrentVersion(String running) throws MalformedURLException, IOException {
		String page = IOUtils.toString(new URL(CURRENT_VERSION_PAGE_URL + running).openStream());
		ByteArrayInputStream bin = new ByteArrayInputStream(page.getBytes());
		Properties p = new Properties();
		p.load(bin);
		String result = p.getProperty("currentVersion", "?");
		return result;

	}

	/**
	 * Check version of running application. Version is read from /META-INF/MANIFEST.MF file
	 * @return
	 * @throws IOException
	 */
	public static String getRunningVersion() throws IOException {
		LOGGER.info("Checking running version");
		String result = null;

		InputStream resourceAsStream = VersionUtil.class.getResourceAsStream("/META-INF/MANIFEST.MF");
		try {
			Manifest manifest = new Manifest(resourceAsStream);
			result = manifest.getMainAttributes().getValue(IMPLEMENTATION_VERSION);
			LOGGER.info("Running version is " + result);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
		}
		if (result == null) {
			result = "";
		}

		return result;
	}

}
