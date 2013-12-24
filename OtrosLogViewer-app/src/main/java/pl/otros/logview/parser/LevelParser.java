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

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class LevelParser {

  private HashMap<String, Level> levels;

  public LevelParser(Locale locale) {
    levels = new HashMap<String, Level>(8);
    ResourceBundle rb = ResourceBundle.getBundle("pl.otros.logview.parser.Levels", locale);
    levels.put(rb.getString("FINEST"), Level.FINEST);
    levels.put(rb.getString("FINER"), Level.FINER);
    levels.put(rb.getString("FINE"), Level.FINE);
    levels.put(rb.getString("INFO"), Level.INFO);
    levels.put(rb.getString("CONFIG"), Level.CONFIG);
    levels.put(rb.getString("WARNING"), Level.WARNING);
    levels.put(rb.getString("SEVERE"), Level.SEVERE);
    levels.put(rb.getString("TRACE"), Level.FINEST);
    levels.put(rb.getString("DEBUG"), Level.FINE);
    levels.put(rb.getString("INFO"), Level.INFO);
    levels.put(rb.getString("WARN"), Level.WARNING);
    levels.put(rb.getString("ERROR"), Level.SEVERE);
    levels.put(rb.getString("FATAL"), Level.SEVERE);
  }

  public Level parse(String string) {
    return levels.get(string);
  }

}
