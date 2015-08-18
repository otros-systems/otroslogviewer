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
package pl.otros.logview.importer;

import pl.otros.logview.LogDataCollector;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.pluginable.PluginableElement;

import javax.swing.*;
import java.io.InputStream;
import java.util.Properties;

public interface LogImporter extends PluginableElement {

  int LOG_IMPORTER_VERSION_1 = 1;

  String PARSER_CLASS = "parser.class";

  String PARSER_DISPLAYABLE_NAME = "parser.displayableName";
  String PARSER_MNEMONIC = "parser.mnemonic";
  String PARSER_KEY_STROKE_ACCELELATOR = "parser.keyStrokeAccelelator";
  String PARSER_ICON = "parser.icon";

  void init(Properties properties) throws InitializationException;

  /**
   * Initialize parsing context specific resources, which are not thread safe (i.e. DateFormat)
   * 
   * @param parsingContext
   */
  void initParsingContext(ParsingContext parsingContext);

  void importLogs(InputStream in, LogDataCollector dataCollector, ParsingContext parsingContext);

  String getKeyStrokeAccelelator();

  int getMnemonic();

  Icon getIcon();
}
