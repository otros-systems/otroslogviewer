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

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public class StartApp {

  private static final Logger LOGGER = Logger.getLogger(StartApp.class.getName());

  public static void main(String[] args) {


    final String runningJava = System.getProperty("java.version");
    final JavaVersion javaVersion = JavaVersion.fromString(runningJava);

    final int compareTo = javaVersion.compareTo(new JavaVersion(1, 8, 0));
    if (compareTo < 0) {
      System.err.println("Java version have to be at least 1.8, you version is " + runningJava);
      JOptionPane.showMessageDialog(null, "Java version have to at least 1.8, your version is " + runningJava, "Java is too old", JOptionPane.ERROR_MESSAGE);
    }

    String classToStart = "pl.otros.logview.gui.LogViewMainFrame";
    int errorCode = 0;
    try {
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
      Class<?> aClass = urlClassLoader.loadClass(classToStart);
      Method method = aClass.getMethod("main", String[].class);
      method.invoke(null, (Object) args); // static method doesn't have an instance

    } catch (ClassNotFoundException e) {
      System.err.printf("Can't load class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      showError(e);
      errorCode = 1;
    } catch (InvocationTargetException e) {
      System.err.printf("Can't invoke method main on class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      showError(e);
      errorCode = 2;
    } catch (IllegalAccessException e) {
      System.err.printf("Can't invoke method main on class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      showError(e);
      errorCode = 3;
    } catch (NoSuchMethodException e) {
      System.err.printf("Can't find method main on class %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      showError(e);
      errorCode = 4;
    } catch (Exception e){
      System.err.printf("Can't start application %s: %s", classToStart, e.getMessage());
      e.printStackTrace();
      showError(e);
      errorCode = 5;
    }
    if (errorCode != 0) {
      System.exit(errorCode);
    }

  }

  private static void showError(Exception exception){
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    exception.printStackTrace(new PrintStream(outputStream));
    JTextArea textArea = new JTextArea("Can't start OtrosLogViewer\n" + new String(outputStream.toByteArray()));
    textArea.setEditable(false);
    JOptionPane.showMessageDialog(null,new JScrollPane(textArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS));

  }
}
