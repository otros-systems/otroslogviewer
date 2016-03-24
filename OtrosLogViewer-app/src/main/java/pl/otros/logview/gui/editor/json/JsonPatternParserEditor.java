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
package pl.otros.logview.gui.editor.json;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.gui.editor.LogPatternParserEditorBase;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.LogParser;
import pl.otros.logview.parser.json.JsonExtractor;
import pl.otros.logview.parser.json.JsonLogParser;
import pl.otros.swing.suggest.StringInsertSuggestionListener;
import pl.otros.swing.suggest.SuggestDecorator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.*;

public class JsonPatternParserEditor extends LogPatternParserEditorBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonPatternParserEditor.class.getName());
  public static final String JSON = "json";

  private final JsonXpathSuggestionSource suggestionSource;

  public JsonPatternParserEditor(OtrosApplication otrosApplication, String logPatternText) {
    super(otrosApplication, logPatternText);
    logFileContent.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateSuggestionSource();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateSuggestionSource();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateSuggestionSource();
      }
    });

    suggestionSource = new JsonXpathSuggestionSource();
    SuggestDecorator.decorate(propertyEditor,
      suggestionSource,
      suggestion -> new JLabel(suggestion.getToDisplay()),
      new StringInsertSuggestionListener());

  }


  private void updateSuggestionSource() {
    final String text = logFileContent.getText();
    final List<String> lines = Splitter.on("\n").trimResults().omitEmptyStrings().splitToList(text);
    StringBuilder sb = new StringBuilder();
    final HashSet<String> keys = new HashSet<>();
    for (String line : lines) {
      if (sb.length() == 0) {
        if (line.trim().startsWith("{")) {
          processLogLine(sb, keys, line);
        }
      } else {
        processLogLine(sb, keys, line);
      }
    }
    LOGGER.debug("Updating json paths with {} elements: {}",keys.size(), Joiner.on(", ").join(keys));
    suggestionSource.setJsonPaths(keys);
    suggestionSource.setJsonDoc(text);
    suggestionSource.setPropertyDoc(propertyEditor.getText());
  }

  private void processLogLine(StringBuilder sb, Set<String> keys, String line) {
    sb.append(line);
    Map<String, String> m = toMap(sb);
    if (m.size() > 0) {
      keys.addAll(m.keySet());
      sb.setLength(0);
    }
  }

  private Map<String, String> toMap(StringBuilder sb) {
    final String text = sb.toString();
    try {
      final JSONObject jsonObject = new JSONObject(text);
      return JsonExtractor.toMap(jsonObject);
    } catch (JSONException e) {
      return new HashMap<>(0);
    }
  }


  @Override
  protected LogParser createLogParser(Properties p) throws InitializationException {
    final String type = p.getProperty("type", "");
    if (!type.equals(JSON)) {
      throw new InitializationException("Property type have to set to \"json\"");
    }
    return new JsonLogParser();
  }


}
