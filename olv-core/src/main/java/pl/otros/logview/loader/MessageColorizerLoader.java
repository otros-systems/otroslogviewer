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
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.gui.message.SoapMessageColorizer;
import pl.otros.logview.gui.message.StackTraceColorizer;
import pl.otros.logview.gui.message.pattern.PropertyPatternMessageColorizer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;

public class MessageColorizerLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageColorizerLoader.class.getName());
  private final BaseLoader baseLoader = new BaseLoader();

  public ArrayList<MessageColorizer> loadInternal(Theme theme) {
    ArrayList<MessageColorizer> list = new ArrayList<>();
    list.add(new SearchResultColorizer(theme));
    list.add(new StackTraceColorizer(theme));
    list.add(new SoapMessageColorizer(theme));
    return list;

  }

  public Collection<MessageColorizer> loadFromJars(File dir) {
    return baseLoader.load(dir, MessageColorizer.class);

  }

  public ArrayList<MessageColorizer> loadFromProperties(File dir, Theme theme) {
    ArrayList<MessageColorizer> list = new ArrayList<>();
    File[] listFiles = dir.listFiles(pathname -> (pathname.isFile() && pathname.getName().endsWith("pattern")));
    if (listFiles != null) {
      for (File file : listFiles) {
        FileInputStream in = null;
        try {
          in = new FileInputStream(file);
          PropertyPatternMessageColorizer colorizer = new PropertyPatternMessageColorizer(theme);
          colorizer.init(in);
          colorizer.setFile(file.getAbsolutePath());
          list.add(colorizer);
        } catch (Exception e) {
          LOGGER.error(String.format("Can't load property file based message colorizer from file %s : %s", file.getName(), e));
        } finally {
          IOUtils.closeQuietly(in);
        }
      }
    }
    return list;

  }
}
