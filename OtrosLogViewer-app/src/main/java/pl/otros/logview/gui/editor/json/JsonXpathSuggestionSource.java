package pl.otros.logview.gui.editor.json;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.parser.json.JsonExtractor;
import pl.otros.swing.suggest.BasicSuggestion;
import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;

class JsonXpathSuggestionSource implements SuggestionSource<BasicSuggestion> {

  public static final String JSON = "json";
  private final List<String> keys;

  public JsonXpathSuggestionSource() {
    keys = new ArrayList<>();
    keys.addAll(Arrays.asList(JsonExtractor.KEYS));
    keys.add("name");
    keys.add("type");
    keys.add("description");
  }

  private Set<String> jsonPaths = new HashSet<>();

  public Set<String> getJsonPaths() {
    return jsonPaths;
  }

  public void setJsonPaths(Set<String> jsonPaths) {
    this.jsonPaths = jsonPaths;
  }

  @Override
  public List<BasicSuggestion> getSuggestions(SuggestionQuery query) {
    final String text = query.getValue();
    //Get last line
    final int caretLocation = query.getCaretLocation();
    String line = lineForPosition(text, caretLocation);
    final boolean matches = line.matches("\\w+\\s*=\\s*.*");
    int position = caretLocation - lineStartIndexAtPosition(text, caretLocation);
    if (line.equalsIgnoreCase("\n")){
      position = position-1;
    }
    if (matches) {
      final int indexOf = line.indexOf("=");
      if (position < indexOf) {
        return getKeysSuggestion(line.substring(0, position), typedProperties(text));
      } else if (position == indexOf) {
        return Collections.singletonList(new BasicSuggestion("=", "="));
      } else {
        return getValuesSuggestions(line, position, indexOf);
      }
    } else if (StringUtils.isBlank(line)) {
      return getKeysSuggestion(StringUtils.EMPTY, typedProperties(text));
    }
    return getKeysSuggestion(line.substring(0, position), typedProperties(text));
  }

  private Set<String> typedProperties(String text) {
    final Properties properties = new Properties();
    try {
      properties.load(new StringReader(text));
    } catch (IOException ignore) {
      //
    }
    return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
  }

  List<BasicSuggestion> getKeysSuggestion(String typedText, Set<String> definedKeys) {
    return keys.stream()
      .filter(s -> !definedKeys.contains(s))
      .filter(s -> s.startsWith(typedText))
      .map(s -> new BasicSuggestion(s, s.substring(typedText.length())+"="))
      .sorted()
      .collect(Collectors.toList());
  }

  List<BasicSuggestion> getValuesSuggestions(String line, int position, int indexOf) {
    String key = line.substring(0, indexOf);
    final String entered = line.substring(indexOf + 1, max(position, indexOf + 1));
    if (key.equals("type")) {
      return suggestionsForTypeJson(line, position);
    } else if (key.equals(JsonExtractor.MDC_KEYS)) {
      return suggestionsForMdc(line, position);
    } else {
      return jsonPaths.stream()
        .filter(s -> s.startsWith(entered))
        .map(s -> new BasicSuggestion(s, s.substring(entered.length())))
        .sorted()
        .collect(Collectors.toList());
    }
  }

  List<BasicSuggestion> suggestionsForMdc(String line, int position) {
    final List<String> usedFields = Splitter.on(",").trimResults().splitToList(line.replaceFirst(".*?=", ""));
    final ArrayList<String> toUse = new ArrayList<>(jsonPaths);
    toUse.removeAll(usedFields);

    String prefix = line.substring(0, position).replaceFirst(".*?=", "").replaceFirst(".*,\\s*", "");

    final List<String> collect = toUse.stream()
      .filter(s -> s.startsWith(prefix))
      .collect(Collectors.toList());

    final List<BasicSuggestion> suggestions = collect.stream()
      .map(s -> new BasicSuggestion(s, s.substring(prefix.length()) + ", "))
      .sorted()
      .collect(Collectors.toList());
    if (jsonPaths.contains(prefix)) {
      suggestions.add(new BasicSuggestion(", ", ", "));
    }
    return suggestions;

  }

  List<BasicSuggestion> suggestionsForTypeJson(String line, int position) {
    final String substring1 = line.substring(0, position);
    final String substring = substring1.replaceFirst(".*?=", "").trim();
    final String toInsert = StringUtils.removeStart(JSON, substring);
    return Collections.singletonList(new BasicSuggestion(JSON, toInsert));
  }

  String lineForPosition(String text, int position) {
    int start = max(0, text.substring(0, min(text.length(), position)).lastIndexOf('\n'));
    int end = text.indexOf('\n', position);
    if (end == -1) {
      end = text.length();
    }
    final String line = text.substring(start, end).replaceAll("\r", "").replace("\n", "");
    return line;
  }

  private int lineStartIndexAtPosition(String text, int position) {
    String textBefore = text.substring(0, min(position,text.length()));
    return Math.max(textBefore.lastIndexOf('\n') + 1, 0);
  }
}
