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

import pl.otros.logview.gui.LogViewMainFrame;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class StartApp {

//  private static final Logger LOGGER = Logger.getLogger(StartApp.class.getName());

  public static void main(String[] args) {

//NOTE: I don't think this code will ever execute, since an app compiled with a java version newer than the installed in the system, would not be able to run and would throw a version error
//    final String runningJava = System.getProperty("java.version");
//    final JavaVersion javaVersion = JavaVersion.fromString(runningJava);
//
//    final int compareTo = javaVersion.compareTo(new JavaVersion(1, 8, 0));
//    if (compareTo < 0) {
//      System.err.println("Java version have to be at least 1.8, you version is " + runningJava);
//      JOptionPane.showMessageDialog(null, "Java version have to at least 1.8, your version is " + runningJava, "Java is too old", JOptionPane.ERROR_MESSAGE);
//    }

    int errorCode = 0;
    try {
      LogViewMainFrame.main(args);

    } catch (Exception e){
      System.err.printf("Can't start application: %s", e.getMessage());
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
    JTextArea textArea = new JTextArea("Can't start OtrosLogViewer\n" + new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
    textArea.setEditable(false);
    JOptionPane.showMessageDialog(null,new JScrollPane(textArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS));

  }
}
