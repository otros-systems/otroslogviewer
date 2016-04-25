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

import com.google.common.base.Splitter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.BaseLoader;
import pl.otros.logview.api.pluginable.AutomaticMarker;
import pl.otros.logview.gui.markers.PropertyFileAbstractMarker;
import pl.otros.logview.gui.markers.RegexMarker;
import pl.otros.logview.gui.markers.StringMarker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class AutomaticMarkerLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticMarker.class.getName());
  private static final BaseLoader baseLoader = new BaseLoader();

  public static ArrayList<AutomaticMarker> loadInternalMarkers() throws IOException {


    ArrayList<AutomaticMarker> markers = new ArrayList<>();
    Properties p = new Properties();
    p.load(AutomaticMarkerLoader.class.getClassLoader().getResourceAsStream("markers.properties"));
    final Iterable<String> defaultMarkers = Splitter.on(',').split(p.getProperty("defaultMarkers"));
    for (String line : defaultMarkers) {
      try {
        Class<?> c = AutomaticMarkerLoader.class.getClassLoader().loadClass(line);
        AutomaticMarker am = (AutomaticMarker) c.newInstance();
        markers.add(am);
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
        LOGGER.error("Error loading class " + line, e);
      }
    }

    return markers;

  }

  public static ArrayList<AutomaticMarker> load(File dir) {
    ArrayList<AutomaticMarker> markers = new ArrayList<>();
    markers.addAll(baseLoader.load(dir, AutomaticMarker.class));
    return markers;
  }

  public static ArrayList<AutomaticMarker> loadRegexMarkers(File dir) {
    ArrayList<AutomaticMarker> markers = new ArrayList<>();
    File[] files = dir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".regexMarker"));
    if (files != null) {
      for (File file : files) {
        try {
          markers.add(loadRegexMarkerFromProperties(file));
        } catch (Exception e) {
          LOGGER.error("Cannot initialize RegexMarker from file " + file.getName(), e);
        }
      }
    }
    return markers;
  }

  public static ArrayList<AutomaticMarker> loadStringMarkers(File dir) {
    ArrayList<AutomaticMarker> markers = new ArrayList<>();
    File[] files = dir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".stringMarker"));
    if (files != null) {
      for (File file : files) {
        try {
          markers.add(loadStringMarkerFromProperties(file));
        } catch (Exception e) {
          LOGGER.error("Cannot initialize StringMarker from file " + file.getName(), e);
        }
      }
    }
    return markers;
  }

  private static AutomaticMarker loadRegexMarkerFromProperties(File file) throws Exception {
    Properties p = new Properties();
    FileInputStream is = null;
    RegexMarker marker = null;
    try {
      is = new FileInputStream(file);
      p.load(is);
      marker = new RegexMarker(p);
      marker.setFileName(file.getName());
    } catch (Exception e) {
      throw e;
    } finally {
      IOUtils.closeQuietly(is);
    }

    return marker;
  }

  private static AutomaticMarker loadStringMarkerFromProperties(File file) throws Exception {
    Properties p = new Properties();
    FileInputStream is = null;
    StringMarker marker = null;
    try {
      is = new FileInputStream(file);
      p.load(is);
      marker = new StringMarker(p);
      marker.setFileName(file.getName());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(is);
    }

    return marker;
  }

  public static AutomaticMarker loadPropertyBasedMarker(Properties p) throws Exception {
    String type = p.getProperty(PropertyFileAbstractMarker.TYPE, "");
    AutomaticMarker marker = null;
    if (type.equalsIgnoreCase(PropertyFileAbstractMarker.TYPE_STRING)) {
      marker = new StringMarker(p);
    } else if (type.equalsIgnoreCase(PropertyFileAbstractMarker.TYPE_REGEX)) {
      marker = new RegexMarker(p);
    }
    if (marker == null) {
      throw new Exception("Unknown type of marker: " + type);
    }
    return marker;
  }

  public static Collection<? extends AutomaticMarker> loadPatternMarker(File dir) {
    ArrayList<AutomaticMarker> markers = new ArrayList<>();
    File[] files = dir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".marker"));
    if (files != null) {
      for (File file : files) {
        FileInputStream fin = null;
        try {
          Properties p = new Properties();
          fin = new FileInputStream(file);
          p.load(fin);
          markers.add(loadPropertyBasedMarker(p));
        } catch (Exception e) {
          LOGGER.error("Cannot initialize RegexMarker from file " + file.getName(), e);
        } finally {
          IOUtils.closeQuietly(fin);
        }
      }
    }
    return markers;
  }

}
