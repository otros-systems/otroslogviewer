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
package pl.otros.logview.loader;

import pl.otros.logview.importer.*;
import pl.otros.logview.importer.log4jxml.Log4jXmlLogImporter;
import pl.otros.logview.parser.JulSimpleFormmaterParser;
import pl.otros.logview.parser.LogParser;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LogImportersLoader {

  public static final Logger LOGGER = Logger.getLogger(LogImportersLoader.class.getName());
  private BaseLoader baseLoader = new BaseLoader();

  public Collection<LogImporter> loadInternalLogImporters() throws InitializationException {
    ArrayList<LogImporter> list = new ArrayList<LogImporter>();

    Properties p = new Properties();

    UtilLoggingXmlLogImporter xmlLogImporter2 = new UtilLoggingXmlLogImporter();
    xmlLogImporter2.init(p);
    list.add(xmlLogImporter2);

    JulSimpleFormmaterParser julSimpleFormmaterParser = new JulSimpleFormmaterParser();
    LogImporterUsingParser julImporter = new LogImporterUsingParser(julSimpleFormmaterParser);
    julImporter.init(p);
    list.add(julImporter);

    Log4jXmlLogImporter log4jXmlLogImporter = new Log4jXmlLogImporter();
    log4jXmlLogImporter.init(new Properties());
    list.add(log4jXmlLogImporter);

    Log4jSerilizedLogImporter log4jSerilizedLogImporter = new Log4jSerilizedLogImporter();
    log4jSerilizedLogImporter.init(new Properties());
    list.add(log4jSerilizedLogImporter);

    return list;

  }

  public Collection<LogImporter> load(File dir) {
    Set<LogImporter> logImporters = new HashSet<LogImporter>();
    if (!dir.exists()) {
      // dir not exist!
      return new ArrayList<LogImporter>();
    }
    File[] files = dir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File pathname) {
        if (pathname.isDirectory() || pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".zip")) {
          return true;
        }
        return false;
      }

    });
    for (int i = 0; i < files.length; i++) {
      Collection<LogImporter> m = null;
      try {
        if (files[i].isDirectory()) {
          m = loadFromDir(files[i]);
        } else {
          m = loadFromJar(files[i]);
        }
        logImporters.addAll(m);

      } catch (IOException e) {
        // TODO Auto-generated catch block
        LOGGER.log(Level.SEVERE, "IOException", e);
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        LOGGER.log(Level.SEVERE, "ClassNotFoundException", e);
      }
    }
    return logImporters;
  }

  private Collection<LogImporter> loadFromJar(File file) throws IOException, ClassNotFoundException {
    ArrayList<LogImporter> importers = new ArrayList<LogImporter>();
    Collection<LogImporter> implementationClasses = baseLoader.loadFromJar(file, LogImporter.class);
    for (LogImporter logImporter : implementationClasses) {
      importers.add(logImporter);
    }

    Collection<LogParser> logParsers = baseLoader.loadFromJar(file, LogParser.class);
    for (LogParser logParser : logParsers) {
      LogImporterUsingParser l = new LogImporterUsingParser(logParser);
      importers.add(l);
    }

    return importers;
  }

  public Collection<LogImporter> loadPropertyPatternFileFromDir(File dir)
  throws InitializationException {
    ArrayList<LogImporter> logImporters = new ArrayList<LogImporter>();
    File[] listFiles = dir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File pathname) {
        return (pathname.isFile() && pathname.getName().endsWith("pattern"));
      }

    });
    if (listFiles != null) {
      StringBuilder exceptionMessages = new StringBuilder();
      for (File file : listFiles) {
        try {
          Properties p = new Properties();
          Log4jPatternMultilineLogParser parser = new Log4jPatternMultilineLogParser();
          p.load(new FileInputStream(file));
          parser.getParserDescription().setFile(file.getAbsolutePath());
          if (p.getProperty(Log4jPatternMultilineLogParser.PROPERTY_TYPE, "").equals("log4j")) {
            LogImporterUsingParser logImporter = new LogImporterUsingParser(parser);
            logImporter.init(p);
            logImporters.add(logImporter);
          }
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE,
                  "Can't load property file based logger [" + file.getName() + ": " + e.getMessage(), e);
          if (exceptionMessages.length() > 0) exceptionMessages.append("\n");
          exceptionMessages.append("Can't load property file based logger [")
                  .append(file.getName()).append(": ").append(e.getMessage());
        }
      }
      if (exceptionMessages.length() > 0)
        throw new InitializationException(exceptionMessages.toString());
    }
    return logImporters;
  }

  // TODO
  private ArrayList<LogImporter> loadFromDir(File file) {

    return new ArrayList<LogImporter>();
  }
}
