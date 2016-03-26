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

import org.apache.commons.io.IOUtils;
import pl.otros.logview.api.BaseLoader;
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.gui.message.SoapMessageColorizer;
import pl.otros.logview.gui.message.StackTraceColorizer;
import pl.otros.logview.gui.message.pattern.PropertyPatternMessageColorizer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageColorizerLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageColorizerLoader.class.getName());
  private final BaseLoader baseLoader = new BaseLoader();

  public ArrayList<MessageColorizer> loadInternal() {
    ArrayList<MessageColorizer> list = new ArrayList<>();
    list.add(new SearchResultColorizer());
    list.add(new StackTraceColorizer());
    list.add(new SoapMessageColorizer());
    return list;

  }

  public Collection<MessageColorizer> loadFromJars(File dir) {
    return baseLoader.load(dir, MessageColorizer.class);

  }

  public ArrayList<MessageColorizer> loadFromProperies(File dir) {
    ArrayList<MessageColorizer> list = new ArrayList<>();
    File[] listFiles = dir.listFiles(pathname -> (pathname.isFile() && pathname.getName().endsWith("pattern")));
    if (listFiles != null) {
      for (File file : listFiles) {
        FileInputStream in = null;
        try {
          in = new FileInputStream(file);
          PropertyPatternMessageColorizer colorizer = new PropertyPatternMessageColorizer();
          colorizer.init(in);
          colorizer.setFile(file.getAbsolutePath());
          list.add(colorizer);
        } catch (Exception e) {
          LOGGER.error(String.format("Can't load property file based message colorizer from file %s : %s", file.getName(), e.getMessage()));
          e.printStackTrace();
        } finally {
          IOUtils.closeQuietly(in);
        }
      }
    }
    return list;

  }
}
