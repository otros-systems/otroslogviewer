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

package pl.otros.logview.api.io;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.reader.ProxyLogDataCollector;
import pl.otros.vfs.browser.util.VFSUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

public class Utils {

  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());

  public static Optional<LogImporter> detectLogImporter(Collection<LogImporter> importers, byte[] buff) {
    return detectPossibleLogImporter(importers, buff).getLogImporter();
  }

  public static PossibleLogImporters detectPossibleLogImporter(Collection<LogImporter> importers, byte[] buff) {
    PossibleLogImporters possibleLogImporters = new PossibleLogImporters();
    int messageImported = 0;
    for (LogImporter logImporter : importers) {
      ByteArrayInputStream bin = new ByteArrayInputStream(buff);
      ProxyLogDataCollector logCollector = new ProxyLogDataCollector();

      ParsingContext parsingContext = new ParsingContext();
      try {
        logImporter.initParsingContext(parsingContext);
      } catch (Exception e) {
        LOGGER.warn(String.format("Exception when initializing parsing context for logger %s: %s", logImporter.getName(), e.getMessage()));
      }
      try {
        logImporter.importLogs(bin, logCollector, parsingContext);
      } catch (Exception e1) {
        // Some log parser can throw exception, due to incomplete line
      }
      int currentLogMessageImported = logCollector.getLogData().length;
      if (messageImported < currentLogMessageImported) {
        messageImported = currentLogMessageImported;
        possibleLogImporters.setLogImporter(Optional.of(logImporter));
      }
      if (currentLogMessageImported > 0) {
        possibleLogImporters.getAvailableImporters().add(logImporter);
      }
    }
    return possibleLogImporters;

  }

  public static void closeQuietly(FileObject fileObject) {

    if (fileObject != null) {
      String friendlyURI = fileObject.getName().getFriendlyURI();
      try {
        LOGGER.info(String.format("Closing file %s", friendlyURI));
        fileObject.close();
        LOGGER.info(String.format("File %s closed", friendlyURI));
      } catch (FileSystemException ignore) {
        LOGGER.error(String.format("File %s is not closed: %s", friendlyURI, ignore.getMessage()));
      }
    }
  }

  /**
   * Get short name for URL
   *
   * @param fileObject File object
   * @return scheme://hostWithoutDomain/fileBaseName
   */
  public static String getFileObjectShortName(FileObject fileObject) {
    StringBuilder sb = new StringBuilder();
    try {
      URI uri = new URI(fileObject.getName().getURI());
      String scheme = fileObject.getName().getScheme();
      sb.append(scheme);
      sb.append("://");
      if (!"file".equals(scheme)) {
        String host = uri.getHost();
        if (host == null)
          host = uri.getAuthority();
        // if host name is not IP, return only host name
        if (host != null && !Pattern.matches("(\\d+\\.){3}\\d+", host)) {
          host = host.split("\\.")[0];
        }
        sb.append(host == null ? "???" : host).append('/');
      }
      sb.append(fileObject.getName().getBaseName());
    } catch (URISyntaxException e) {
      LOGGER.warn("Problem with preparing short name of FileObject: " + e.getMessage());
      sb.setLength(0);
      sb.append(fileObject.getName().getScheme()).append("://").append(fileObject.getName().getBaseName());
    }
    return sb.toString();
  }

  /**
   * Create temporary FileObject with content. Implementation will create file in temprary directory
   *
   * @param content File content
   * @return FileObject
   * @throws IOException in case of IO problems
   */
  public static FileObject createFileObjectWithContent(String content) throws IOException {
    final File tempFile = File.createTempFile("olv_temp", "");
    OutputStream out = new FileOutputStream(tempFile);
    IOUtils.write(content, out, Charset.forName("UTF-8"));
    IOUtils.closeQuietly(out);
    return VFSUtils.resolveFileObject(tempFile.toURI());
  }
}
