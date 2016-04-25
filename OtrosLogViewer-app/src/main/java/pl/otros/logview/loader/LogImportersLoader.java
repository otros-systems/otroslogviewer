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
package pl.otros.logview.loader;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.BaseLoader;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.parser.LogParser;
import pl.otros.logview.importer.Log4jSerilizedLogImporter;
import pl.otros.logview.importer.UtilLoggingXmlLogImporter;
import pl.otros.logview.importer.log4jxml.Log4jXmlLogImporter;
import pl.otros.logview.importer.logback.LogbackSocketLogImporter;
import pl.otros.logview.parser.JulSimpleFormatterParser;
import pl.otros.logview.parser.json.JsonLogParser;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.logview.parser.log4j.Log4jUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LogImportersLoader {

  public static final Logger LOGGER = LoggerFactory.getLogger(LogImportersLoader.class.getName());
  private final BaseLoader baseLoader = new BaseLoader();

  public Collection<LogImporter> loadInternalLogImporters() throws InitializationException {
    ArrayList<LogImporter> list = new ArrayList<>();

    Properties p = new Properties();

    UtilLoggingXmlLogImporter xmlLogImporter2 = new UtilLoggingXmlLogImporter();
    xmlLogImporter2.init(p);
    list.add(xmlLogImporter2);

    JulSimpleFormatterParser julSimpleFormmaterParser = new JulSimpleFormatterParser();
    LogImporterUsingParser julImporter = new LogImporterUsingParser(julSimpleFormmaterParser);
    julImporter.init(p);
    list.add(julImporter);

    Log4jXmlLogImporter log4jXmlLogImporter = new Log4jXmlLogImporter();
    log4jXmlLogImporter.init(new Properties());
    list.add(log4jXmlLogImporter);

    Log4jSerilizedLogImporter log4jSerilizedLogImporter = new Log4jSerilizedLogImporter();
    log4jSerilizedLogImporter.init(new Properties());
    list.add(log4jSerilizedLogImporter);

    LogbackSocketLogImporter logbackSockeLogImporter = new LogbackSocketLogImporter();
    logbackSockeLogImporter.init(new Properties());
    list.add(logbackSockeLogImporter);

    return list;

  }

  public Collection<LogImporter> load(File dir) {
    Set<LogImporter> logImporters = new HashSet<>();
    if (!dir.exists()) {
      // dir not exist!
      return new ArrayList<>();
    }
    File[] files = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".zip"));
    for (File file : files) {
      Collection<LogImporter> m;
      try {
        if (file.isDirectory()) {
          m = loadFromDir(file);
        } else {
          m = loadFromJar(file);
        }
        logImporters.addAll(m);

      } catch (IOException e) {
        // TODO Auto-generated catch block
        LOGGER.error("IOException", e);
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        LOGGER.error("ClassNotFoundException", e);
      }
    }
    return logImporters;
  }

  private Collection<LogImporter> loadFromJar(File file) throws IOException, ClassNotFoundException {
    ArrayList<LogImporter> importers = new ArrayList<>();
    Collection<LogImporter> implementationClasses = baseLoader.loadFromJar(file, LogImporter.class);
    importers.addAll(implementationClasses.stream().collect(Collectors.toList()));

    Collection<LogParser> logParsers = baseLoader.loadFromJar(file, LogParser.class);
    for (LogParser logParser : logParsers) {
      LogImporterUsingParser l = new LogImporterUsingParser(logParser);
      importers.add(l);
    }

    return importers;
  }

  public Collection<LogImporter> loadPropertyPatternFileFromDir(File dir)
    throws InitializationException {
    ArrayList<LogImporter> logImporters = new ArrayList<>();
    File[] listFiles = dir.listFiles(pathname -> (pathname.isFile() && pathname.getName().endsWith("pattern")));
    if (listFiles != null) {
      StringBuilder exceptionMessages = new StringBuilder();
      for (File file : listFiles) {
        FileInputStream is = null;
        try {
          Properties p = new Properties();
          Log4jPatternMultilineLogParser parser = new Log4jPatternMultilineLogParser();
          is = new FileInputStream(file);
          p.load(is);
          parser.getParserDescription().setFile(file.getAbsolutePath());
          if (p.getProperty(Log4jPatternMultilineLogParser.PROPERTY_TYPE, "").equals("log4j")) {
            LogImporterUsingParser logImporter = new LogImporterUsingParser(parser);
            logImporter.init(p);
            logImporters.add(logImporter);
          } else if (p.getProperty(Log4jPatternMultilineLogParser.PROPERTY_TYPE, "").equals("log4j-native")) {
            parser.getParserDescription().setFile(file.getAbsolutePath());
            Log4jUtil.parsePattern(p);
            LogImporterUsingParser logImporter = new LogImporterUsingParser(parser);
            logImporter.init(p);
            logImporters.add(logImporter);
          } else if (p.getProperty(Log4jPatternMultilineLogParser.PROPERTY_TYPE, "").equals("json")) {
            final JsonLogParser jsonLogParser = new JsonLogParser();
            jsonLogParser.init(p);
            logImporters.add(new LogImporterUsingParser(jsonLogParser));
          } else {
            LOGGER.error("Unknown log type: " + p.getProperty(Log4jPatternMultilineLogParser.PROPERTY_TYPE, ""));
          }
        } catch (Exception e) {
          LOGGER.error(
            "Can't load property file based logger [" + file.getName() + ": " + e.getMessage(), e);
          if (exceptionMessages.length() > 0) exceptionMessages.append("\n");
          exceptionMessages.append("Can't load property file based logger [")
            .append(file.getName()).append(": ").append(e.getMessage());
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
      if (exceptionMessages.length() > 0)
        throw new InitializationException(exceptionMessages.toString());
    }
    return logImporters;
  }

  // TODO
  private ArrayList<LogImporter> loadFromDir(File dir) {
    LOGGER.debug("Will not load from dir {}, method is not implemented", dir.getAbsolutePath());
    return new ArrayList<>();
  }
}
