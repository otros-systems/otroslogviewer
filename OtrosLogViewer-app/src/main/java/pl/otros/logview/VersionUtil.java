/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Manifest;

public class VersionUtil {

  private static final String CURRENT_VERSION_PAGE_URL = "http://otroslogviewer.appspot.com/services/currentVersion?";
  private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtil.class.getName());
  public static final String IMPLEMENTATION_VERSION = "Implementation-Version";

  /**
   * Check latest released version.
   *
   * @param running  currently running version
   * @return Latest released version
   * @throws IOException
   */
  public static String getCurrentVersion(String running, Proxy proxy, OtrosApplication otrosApplication) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("runningVersion=").append(running);
    sb.append("&java.version=").append(URLEncoder.encode(System.getProperty("java.version"), "ISO-8859-1"));
    sb.append("&os.name=").append(URLEncoder.encode(System.getProperty("os.name"), "ISO-8859-1"));
    sb.append("&vm.vendor=").append(URLEncoder.encode(System.getProperty("java.vm.vendor"), "ISO-8859-1"));
    String uuid = URLEncoder.encode(otrosApplication.getConfiguration().getString(ConfKeys.UUID, ""), "ISO-8859-1");
    sb.append("&uuid=").append(uuid);
    URL url = new URL(CURRENT_VERSION_PAGE_URL + sb.toString());
    String page = IOUtils.toString(url.openConnection(proxy).getInputStream());
    ByteArrayInputStream bin = new ByteArrayInputStream(page.getBytes());
    Properties p = new Properties();
    p.load(bin);
    return p.getProperty("currentVersion", "?");

  }

  /**
   * Check version of running application. Version is read from /META-INF/MANIFEST.MF file
   *
   * @return currently running version
   * @throws IOException
   */
  public static String getRunningVersion() throws IOException {
    LOGGER.info("Checking running version");
    String result = "";
    Enumeration<URL> resources = VersionUtil.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      if (url.toString().contains("OtrosLogViewer")) {
        InputStream inputStream = url.openStream();
        try {
          Manifest manifest = new Manifest(inputStream);
          result = manifest.getMainAttributes().getValue(IMPLEMENTATION_VERSION);
          LOGGER.info("Running version is " + result);

        } finally {
          IOUtils.closeQuietly(inputStream);
        }
      }
    }
    return result;
  }

}
