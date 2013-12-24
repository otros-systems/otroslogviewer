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
package pl.otros.logview.parser;

import pl.otros.logview.LogData;
import pl.otros.logview.importer.InitializationException;

import java.text.ParseException;
import java.util.Properties;

public interface LogParser {

  public static int LOG_PARSER_VERSION_1 = 1;

  public void init(Properties properties) throws InitializationException;

  public void initParsingContext(ParsingContext parsingContext);

  public LogData parse(String line, ParsingContext parsingContext) throws ParseException;

  public ParserDescription getParserDescription();

  public int getVersion();
}
