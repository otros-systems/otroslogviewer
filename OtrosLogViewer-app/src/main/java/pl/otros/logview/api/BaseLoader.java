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
package pl.otros.logview.api;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class BaseLoader {

  public static final Logger LOGGER = LoggerFactory.getLogger(BaseLoader.class.getName());

  /**
   * Load classes (interface implementation) from directory.
   *
   * @param <T>
   * @param dir
   *          dir with classes, zip or jar's
   * @param type
   *          interface to load
   * @return
   */
  public <T> Collection<T> load(File dir, Class<T> type) {
    Set<T> logImporters = new HashSet<>();
    if (!dir.exists()) {
      // dir not exist!
      return new ArrayList<>();
    }
    File[] files = dir.listFiles(pathname -> {
      return pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".zip");
    });
    logImporters.addAll(loadFromDir(dir, type));
    for (File file : files) {
      logImporters.addAll(loadFromJar(file, type));
    }
    return logImporters;
  }

  public <T> Collection<T> loadFromDir(File file, Class<T> type) {
    ArrayList<T> list = new ArrayList<>();
    try {
      List<Class<T>> implementationClasses = getInterfaceImplementations(type, file);

      for (Class<?> class1 : implementationClasses) {
        try {
          T classInstance = (T) class1.newInstance();
          list.add(classInstance);
        } catch (Throwable e) {
          LOGGER.error(String.format("Error creating class %s from file %s: %s", class1.getName(), file, e.getMessage()));
        }
      }
    } catch (Throwable e) {
      LOGGER.error(String.format("Error loading class type %s from file %s: %s", type, file, e.getMessage()));
    }
    return list;
  }

  public <T> Collection<T> loadFromJar(File file, Class<T> type) {
    ArrayList<T> list = new ArrayList<>();
    try {
      List<Class<T>> implementationClasses = getInterfaceImplementations(type, file);
      for (Class<?> class1 : implementationClasses) {
        try {
          T am = (T) class1.newInstance();
          list.add(am);
        } catch (Throwable e) {
          LOGGER.error(String.format("Error creating class %s from file %s: %s", class1.getName(), file, e.getMessage()));
        }
      }
    } catch (Throwable e) {
      LOGGER.error(String.format("Error loading class type %s from file %s: %s", type, file, e.getMessage()));
    }
    return list;
  }

  public <T> List<Class<T>> getInterfaceImplementations(Class<T> interfaceClass, File f) throws IOException, ClassNotFoundException {
    ArrayList<Class<T>> list = new ArrayList<>();
    List<String> classes = null;
    if (f.isDirectory()) {
      classes = getClassesFromDir(f);
    } else {
      classes = getClassesFromJar(f);
    }
    URL url = f.toURI().toURL();
    ClassLoader cl = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader());
    for (String klazz : classes) {
      try {
        Class<?> c = cl.loadClass(klazz);
        if (interfaceClass.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
          list.add((Class<T>) c);
        }
      } catch (Throwable t) {
        LOGGER.warn(String.format("Error checking if class %s from file %s is implementing %s: %s", klazz, f, interfaceClass.getName(), t.getMessage()));
      }
    }
    return list;

  }

  public List<String> getClassesFromDir(File dir) throws IOException {
    ArrayList<String> list = new ArrayList<>();
    ArrayList<File> fileList = new ArrayList<>();
    findClassesFiles(dir, fileList);
    int length = dir.getAbsolutePath().length();
    for (File file : fileList) {
      String classPath = file.getAbsolutePath().substring(length + 1).replace(".class", "").replace(File.separatorChar, '.');
      list.add(classPath);
    }
    return list;
  }

  private void findClassesFiles(File dir, ArrayList<File> list) {
    File[] listFiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));
    for (File file : listFiles) {
      if (file.isDirectory()) {
        findClassesFiles(file, list);
      } else {
        list.add(file);
      }
    }
  }

  public List<String> getClassesFromJar(File jarFile) throws IOException {
    ArrayList<String> list = new ArrayList<>();
    JarInputStream jarInputStream = null;
    try {
      jarInputStream = new JarInputStream(new FileInputStream(jarFile));
      JarEntry jarEntry;
      while (true) {
        jarEntry = jarInputStream.getNextJarEntry();
        if (null == jarEntry) {
          break;//
        }
        String s = jarEntry.getName();
        if (s.endsWith(".class")) {
          list.add(s.replace('/', '.').substring(0, s.length() - 6));
        }
      }
    } catch (IOException e) {
      throw e;
    } finally {
      IOUtils.closeQuietly(jarInputStream);
    }

    return list;
  }
}
