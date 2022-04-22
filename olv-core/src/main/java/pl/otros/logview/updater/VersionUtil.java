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
package pl.otros.logview.updater;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Optional;
import java.util.jar.Manifest;

public class VersionUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtil.class.getName());
  private static final String IMPLEMENTATION_TITLE="Implementation-Title";
  private static final String IMPLEMENTATION_VERSION = "Implementation-Version";

  private final String currentVersionPageUrl;
  private Optional<String> runningVersion = Optional.empty();

  public VersionUtil() {
    this("https://github.com/otros-systems/otroslogviewer/releases/latest/download/versionInfo.json");
  }

  public VersionUtil(String currentVersionPageUrl) {
    this.currentVersionPageUrl = currentVersionPageUrl;
  }

  /**
   * Check latest released version.
   *
   * @return Latest released version
   * @throws IOException
   */
  public Optional<String> getCurrentVersion(Proxy proxy) throws IOException {
    final String requestUrl = buildRequestUrl();
    LOGGER.debug("Will use URL: {}", requestUrl);
    URL url = new URL(requestUrl);
    String page = IOUtils.toString(url.openConnection(proxy).getInputStream(), StandardCharsets.UTF_8);
    LOGGER.debug("Response from version server is:\n{}", page);
    VersionInformationBean versionInformation = null;
    try {
      versionInformation = JSON.parseObject(page, VersionInformationBean.class);
    } catch (JSONException e) {
      LOGGER.error("Cannot parse version info: '" + page + "'");
    }
    String version = Optional.ofNullable(versionInformation).map(VersionInformationBean::getCurrentVersion).map(VersionBean::toString).orElse(null);
    final Optional<String> s = validateResponse(version);
    LOGGER.info("Current version is: {}", s);
    return s;
  }

  Optional<String> validateResponse(String currentVersion) {
    if (currentVersion != null && currentVersion.matches("\\d+(.\\d+)*")) {
      return Optional.of(currentVersion);
    } else {
      return Optional.empty();
    }
  }

  @Nonnull
  private String buildRequestUrl() {
    return currentVersionPageUrl;
  }

  /**
   * Check version of running application. Version is read from /META-INF/MANIFEST.MF file
   *
   * @return currently running version
   * @throws IOException
   */
  public String getRunningVersion() throws IOException {
    if (!runningVersion.isPresent()) {
      runningVersion = readRunningVersionFromManifest();
    }
    return runningVersion.orElse("");
  }

  private Optional<String> readRunningVersionFromManifest() throws IOException {
    Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
        try (InputStream inputStream = url.openStream()) {
          Manifest manifest = new Manifest(inputStream);
          String implementationTitle = manifest.getMainAttributes().getValue(IMPLEMENTATION_TITLE);
          if(implementationTitle != null && implementationTitle.equals("OtrosLogViewer-app")) {
            String result = manifest.getMainAttributes().getValue(IMPLEMENTATION_VERSION);
            LOGGER.debug("Running version is " + result);
            return Optional.of(result);
          }
        }
    }
    return Optional.empty();
  }

}
