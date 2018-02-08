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
package pl.otros.logview.gui.message;

import org.apache.commons.io.IOUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.api.model.LocationInfo;
import pl.otros.logview.api.pluginable.MessageFragmentStyle;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static pl.otros.logview.gui.message.StackTraceColorizer.STYLE_ATTRIBUTE_EXCEPTION_MSG;

public class StackTraceColorizerTest {

  @Test
  public void testColorizingNeeded() throws IOException {
    //given
    String string = IOUtils.toString(getSystemResourceAsStream("stacktrace/stacktrace.txt"), UTF_8);
    StackTraceColorizer colorizer = new StackTraceColorizer();
    //when
    boolean colorizingNeeded = colorizer.colorizingNeeded(string);
    //then
    AssertJUnit.assertTrue(colorizingNeeded);
  }

  @Test
  public void findExceptionNameAndMessage() throws IOException, BadLocationException {
    //given
    String stacktrace = IOUtils.toString(getSystemResourceAsStream("stacktrace/stacktrace3.txt"), UTF_8).replace("\r", "");
    StackTraceColorizer colorizer = new StackTraceColorizer();
    colorizer.initStyles();
    final Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    //when
    final Collection<MessageFragmentStyle> colorize = colorizer.findExceptionNameAndMessage(style, stacktrace);

    //then
    final List<MessageFragmentStyle> exceptions = colorize.stream()
      .filter(msf -> msf.getStyle().getAttribute(STYLE_ATTRIBUTE_EXCEPTION_MSG) != null)
      .sorted((o1, o2) -> o1.getOffset() - o2.getOffset())
      .collect(Collectors.toList());

    assertEquals(exceptions.size(), 2);
    final MessageFragmentStyle msf0 = exceptions.get(0);
    assertEquals(stacktrace.substring(msf0.getOffset(), msf0.getOffset() + msf0.getLength()), "com.datastax.driver.core.exceptions.TransportException: [null] Cannot connect");
    assertEquals(msf0.getOffset(), 97);
    assertEquals(msf0.getLength(), 77);
    assertEquals(msf0.getStyle().getAttribute(STYLE_ATTRIBUTE_EXCEPTION_MSG), "com.datastax.driver.core.exceptions.TransportException: [null] Cannot connect");
    final MessageFragmentStyle msf1 = exceptions.get(1);
    assertEquals(stacktrace.substring(msf1.getOffset(), msf1.getOffset() + msf1.getLength()), "Caused by: java.nio.channels.UnresolvedAddressException: null");
    assertEquals(msf1.getOffset(), 2540);
    assertEquals(msf1.getLength(), 61);
    assertEquals(msf1.getStyle().getAttribute(STYLE_ATTRIBUTE_EXCEPTION_MSG), "java.nio.channels.UnresolvedAddressException: null");
  }


  @DataProvider(name = "exceptionNameAndMessage")
  public Object[][] exceptionNameAndMessageDataProvider() {
    return new Object[][]{
      new Object[]{"", false},
      new Object[]{"Caused by: java.io.IOException: Connection reset by peer", true},
      new Object[]{"Caused by: java.io.IOException: Error executing request, connection broken.... :)", true},
      new Object[]{"java.lang.RuntimeException: java.util.concurrent.ExecutionException: java.io.IOException: Error executing request, connection broken.... :)", true}
    };

  }

  @Test(dataProvider = "exceptionNameAndMessage")
  public void testExceptionNameAndMessagePattern(String msg, boolean shouldFind) {

    //when
    final Matcher matcher = StackTraceColorizer.exceptionNameAndMessage.matcher(msg);

    //then
    assertEquals(matcher.find(), shouldFind);
  }

  @Test
  public void testColorize() throws IOException, BadLocationException {
    //given
    String string = "something\n" +
      "java.io.FileNotFoundException: fred.txt //comment\n" +
      "\tat java.io.FileInputStream.<init>(FileInputStream.java) //code comment \n" +
      "\tat java.io.FileInputStream.<init>(FileInputStream.java) //code comment\n" +
      "\tat A.ExTest.readMyFile(ExTest.java:19) //Some code\n" +
      "  at ExTest.main(ExTest.java:7)\n";

    StackTraceColorizer colorizer = new StackTraceColorizer();

    //when
    Collection<MessageFragmentStyle> colorize = colorizer.colorize(string);
    //then
//    System.out.println("Substring: \"" + string.substring(137,210)+"\"\n\n");
//    System.out.println("Substring: \"" + string.substring(137,137+7)+"\"\n\n");
//    System.out.println("Substring: \"" + string.substring(145,145+15)+"\"\n\n");
//    System.out.println("Substring: \"" + string.substring(161,161+6)+"\"\n\n");
//    System.out.println("Substring: \"" + string.substring(229,229+14)+"\"\n\n");
    assertEquals(colorize.size(),47);

    assertTrue(colorize.stream().filter(msf -> msf.getOffset() == 137 && msf.getLength() == 7 && msf.getStyle().getName().equals("stylePackage")).findAny().isPresent());
    assertTrue(colorize.stream().filter(msf -> msf.getOffset() == 145 && msf.getLength() == 15 && msf.getStyle().getName().equals("styleClass")).findAny().isPresent());
    assertTrue(colorize.stream().filter(msf -> msf.getOffset() == 161 && msf.getLength() == 6 && msf.getStyle().getName().equals("styleMethod")).findAny().isPresent());
    assertTrue(colorize.stream().filter(msf -> msf.getOffset() == 168 && msf.getLength() == 20 && msf.getStyle().getName().equals("styleFile")).findAny().isPresent());
    assertTrue(colorize.stream().filter(msf -> msf.getOffset() == 189 && msf.getLength() == 15 && msf.getStyle().getName().equals("styleCodeComment")).findAny().isPresent());
    final Optional<MessageFragmentStyle> styleWithLocationOptional = colorize.stream().filter(msf -> msf.getOffset() == 229 && msf.getLength() == 14 && msf.getStyle().getName().startsWith("styleFile-LocationInfo")).findAny();
    assertTrue(styleWithLocationOptional.isPresent());
    final LocationInfo locationInfo = (LocationInfo) styleWithLocationOptional.get().getStyle().getAttribute(StackTraceColorizer.STYLE_ATTRIBUTE_LOCATION_INFO);
    assertEquals(locationInfo.getPackageName(),Optional.of("A"));
    assertEquals(locationInfo.getClassName(),Optional.of("A.ExTest"));
    assertEquals(locationInfo.getMethod(),Optional.of("readMyFile"));
    assertEquals(locationInfo.getFileName(),Optional.of("ExTest.java"));
    assertEquals(locationInfo.getLineNumber(),Optional.of(19));
    assertEquals(locationInfo.getMessage(),Optional.empty());


  }
}
