package pl.otros.logview.gui.editor.json;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.parser.json.JsonExtractor;
import pl.otros.logview.util.DateUtil;
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
  private final DateUtil dateUtil = new DateUtil();

  public JsonXpathSuggestionSource() {
    keys = new ArrayList<>();
    keys.addAll(Arrays.asList(JsonExtractor.KEYS));
    keys.add("name");
    keys.add("type");
    keys.add("description");

  }

  private Set<String> jsonPaths = new HashSet<>();
  private String jsonDoc = "";
  private String propertyDoc = "";

  public void setJsonPaths(Set<String> jsonPaths) {
    this.jsonPaths = jsonPaths;
  }

  public void setJsonDoc(String jsonDoc) {
    this.jsonDoc = jsonDoc;
  }

  public void setPropertyDoc(String propertyDoc) {
    this.propertyDoc = propertyDoc;
  }

  @Override
  public List<BasicSuggestion> getSuggestions(SuggestionQuery query) {
    final String text = query.getValue();
    //Get last line
    final int caretLocation = query.getCaretLocation();
    String line = lineForPosition(text, caretLocation);
    final boolean matches = line.matches("\\w+\\s*=\\s*.*");
    int position = caretLocation - lineStartIndexAtPosition(text, caretLocation);
    if (line.equalsIgnoreCase("\n")) {
      position = position - 1;
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

  protected List<BasicSuggestion> getKeysSuggestion(String typedText, Set<String> definedKeys) {
    return keys.stream()
      .filter(s -> !definedKeys.contains(s))
      .filter(s -> s.startsWith(typedText))
      .map(s -> new BasicSuggestion(s, s.substring(typedText.length()) + "="))
      .sorted()
      .collect(Collectors.toList());
  }

  protected List<BasicSuggestion> getValuesSuggestions(String line, int position, int indexOf) {
    String key = line.substring(0, indexOf);
    final String entered = line.substring(indexOf + 1, max(position, indexOf + 1));
    switch (key) {
      case "type":
        return suggestionsForTypeJson(line, position);
      case JsonExtractor.MDC_KEYS:
        return suggestionsForMdc(line, position);
      case JsonExtractor.DATE_FORMAT:
        Properties p = new Properties();
        try {
          p.load(new StringReader(propertyDoc));
          final String dateProperty = p.getProperty(JsonExtractor.DATE);
          final JsonExtractor jsonExtractor = new JsonExtractor();
          final Set<String> dates = jsonExtractor.extractFieldValues(jsonDoc, dateProperty).stream().limit(5).collect(Collectors.toSet());
          final Set<String> parsedDateFormats =
            dateUtil.matchingDateFormat(dateUtil.allDateFormats(), dates);
          return parsedDateFormats.stream().map(s -> new BasicSuggestion(s, s)).collect(Collectors.toList());
        } catch (IOException e) {
          e.printStackTrace();
        }
        return Collections.singletonList(new BasicSuggestion("Timestamp as long", "timestamp"));
      default:
        return jsonPaths.stream()
          .filter(s -> s.startsWith(entered))
          .map(s -> new BasicSuggestion(s, s.substring(entered.length())))
          .sorted()
          .collect(Collectors.toList());
    }
  }


  protected List<BasicSuggestion> suggestionsForMdc(String line, int position) {
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

  protected List<BasicSuggestion> suggestionsForTypeJson(String line, int position) {
    final String substring1 = line.substring(0, position);
    final String substring = substring1.replaceFirst(".*?=", "").trim();
    final String toInsert = StringUtils.removeStart(JSON, substring);
    return Collections.singletonList(new BasicSuggestion(JSON, toInsert));
  }

  protected String lineForPosition(String text, int position) {
    int start = max(0, text.substring(0, min(text.length(), position)).lastIndexOf('\n'));
    int end = text.indexOf('\n', position);
    if (end == -1) {
      end = text.length();
    }
    return text.substring(start, end).replaceAll("\r", "").replace("\n", "");
  }

  private int lineStartIndexAtPosition(String text, int position) {
    String textBefore = text.substring(0, min(position, text.length()));
    return max(textBefore.lastIndexOf('\n') + 1, 0);
  }


}
