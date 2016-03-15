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
package pl.otros.logview.gui.message;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.apache.commons.io.IOUtils;
import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;

public class StackTraceColorizerTest {
  @Test
  public void testColorizingNeeded() throws IOException {
    //given
    String string = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("stacktrace/stacktrace.txt"));
    StackTraceColorizer colorizer = new StackTraceColorizer();
    //when
    boolean colorizingNeeded = colorizer.colorizingNeeded(string);
    //then
    AssertJUnit.assertTrue(colorizingNeeded);
  }

  @Test
  public void testColorize() throws IOException, BadLocationException {
    //given
    String string = "at A.ExTest.readMyFile(ExTest.java:19)\n  at ExTest.main(ExTest.java:7)\naa";
    string = "something\n" +
             "java.io.FileNotFoundException: fred.txt //comment\n" +
             "\tat java.io.FileInputStream.<init>(FileInputStream.java) //code comment \n" +
             "\tat java.io.FileInputStream.<init>(FileInputStream.java) //code comment\n" +
             "\tat A.ExTest.readMyFile(ExTest.java:19) //Some code\n" +
             "  at ExTest.main(ExTest.java:7)\n";
      string = "Formatted: Error in thread Thread-7\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame$15$1.run(LogViewMainFrame.java:888)  //throw new RuntimeException(\"Exception from tread!\");\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:898)  //KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:890)  //}).start();\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:891)  //}\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:892)  //}\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:893)  //});\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:894)  //menu.add(b);\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:894)  //menu.add(b);\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:895)  //}\n" +
              "\tat pl.otros.logview.gui.LogViewMainFrame.initPosition(LogViewMainFrame.java:896)  //";
    StackTraceColorizer colorizer = new StackTraceColorizer();
    Matcher matcher = StackTraceColorizer.exceptionLine.matcher(string);
//    System.out.println("Find: " + matcher.find());
    while (matcher.find()) {
      for (int i = 0; i <= matcher.groupCount(); i++) {
        System.out.printf("%d: %s%n", i, matcher.group(i));
      }
    }
    //when
    Collection<MessageFragmentStyle> colorize = colorizer.colorize(string);
    //then
    //TODO
    AssertJUnit.assertTrue(colorize.size()>2);

  }
}
