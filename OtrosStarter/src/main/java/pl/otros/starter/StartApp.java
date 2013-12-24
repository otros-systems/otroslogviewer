/*
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
 */

package pl.otros.starter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public class StartApp {

  private static final Logger LOGGER = Logger.getLogger(StartApp.class.getName());

  public static void main(String[] args) throws IOException {
    ClassLoaderResolver flatFileClassLoaderResolver = new FlatFileClassLoaderResolver();
    String olvHome = System.getProperty("OLV_HOME");
    InputStream in = new FileInputStream(new File(olvHome, "classpath.txt"));

    URL[] classPathUrls = flatFileClassLoaderResolver.getClassPathUrls(in);
    LOGGER.info("Added " + classPathUrls.length + " jars to classpath");
    for (URL url : classPathUrls) {
      LOGGER.info("Using classpath: " + url.toExternalForm());
    }

    URLClassLoader urlClassLoader = new URLClassLoader(classPathUrls, StartApp.class.getClassLoader());
    Thread.currentThread().setContextClassLoader(urlClassLoader);
    String classToStart = "pl.otros.logview.gui.LogViewMainFrame";
    int errorCode = 0;
    try {
      Class<?> aClass = urlClassLoader.loadClass(classToStart);
      Method method = aClass.getMethod("main", String[].class);
      method.invoke(null, (Object) args); // static method doesn't have an instance

    } catch (ClassNotFoundException e) {
      System.err.printf("Cant load class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      errorCode = 1;
    } catch (InvocationTargetException e) {
      System.err.printf("Cant invoke method main on class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      errorCode = 2;
    } catch (IllegalAccessException e) {
      System.err.printf("Cant invoke method main on class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      errorCode = 3;
    } catch (NoSuchMethodException e) {
      System.err.printf("Cant find method main on class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      errorCode = 4;
    }
    if (errorCode != 0) {
      System.exit(errorCode);
    }

  }

}
