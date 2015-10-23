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
package pl.otros.logview.gui.editor.log4j;

import pl.otros.logview.gui.editor.LogPatternParserEditorBase;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.parser.LogParser;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;

import java.util.Properties;

public class Log4jPatternParserEditor extends LogPatternParserEditorBase {


  public Log4jPatternParserEditor(OtrosApplication otrosApplication, String logPatternText) {
    super(otrosApplication, logPatternText);

  }

  @Override
  protected LogParser createLogParser(Properties p) throws InitializationException {
    final String type = p.getProperty(Log4jPatternMultilineLogParser.PROPERTY_TYPE);
    if ("log4j".equals(type) || "log4j-native".equals(type)) {
      return new Log4jPatternMultilineLogParser();
    }
    throw new InitializationException("property type have to set to \"json\" or \"log4j\" ");
  }

}
