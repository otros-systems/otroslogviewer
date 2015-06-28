package pl.otros.logview.accept.query.org.apache.log4j.suggestion;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.suggestion.SearchSuggestion;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuerySuggestionSource implements SuggestionSource<SearchSuggestion> {

  private List<String> operators = Splitter.on(" ").splitToList("!= == ~= like exists < > <= >=");
  private List<String> joins = Splitter.on(" ").splitToList("&& ||");
  private List<String> fields = Splitter.on(" ").splitToList("level message class method logger m l");

  private Set<String> fieldsSet = new HashSet<String>(fields);
  private List<String> history;

  public QuerySuggestionSource(List<String> history) {
    this.history = history;
  }

  public enum ExpectedType {
    FIELD,
    OPERATOR,
    VALUE_MSG,
    VALUE_LEVEL,
    VALUE_DATE,
    PROPERTY,
    CLOSE_PARENTHESIS
  }

  @Override
  public List<SearchSuggestion> getSuggestions(String s) {
    if (s.trim().length() == 0) {
      return fields.stream().map(f -> new SearchSuggestion(f, f + " ")).collect(Collectors.toList());
    }
    ExpectedType expectedType = getExpectedType(s);

    if (expectedType == ExpectedType.FIELD) {
      return fields.stream().map(f -> new SearchSuggestion(f, f + " ")).collect(Collectors.toList());
    } else if (expectedType == ExpectedType.OPERATOR) {
      return operators.stream().map(o -> new SearchSuggestion(o, s + " " + o + " ")).collect(Collectors.toList());
    } else if (expectedType == ExpectedType.VALUE_MSG) {
      return new ArrayList<>();
    } else if (expectedType == ExpectedType.PROPERTY) {
      return Collections.singletonList(new SearchSuggestion(")", s + ") "));
    }


    return new ArrayList<>();
  }

  ExpectedType getExpectedType(String s) {
    if (s.trim().length() == 0) {
      return ExpectedType.FIELD;
    }
    final String p = "(" + Joiner.on("|").join(fields) + ")\\s*(" + Joiner.on("|").join(operators) + ")\\s*(.*)";
    final Pattern pattern = Pattern.compile(p);
    System.out.println("Pattern is " + p);
    String rest = s;
    while (rest.matches(p)) {
      System.out.println("rest is: " + rest);
      final Matcher matcher = pattern.matcher(rest);
      final boolean b = matcher.find();
      for (int i = 0; i <= matcher.groupCount(); i++) {
        System.out.println("Group " + i + "=" + matcher.group(i));
      }
      rest = matcher.group(3);
      rest = rest.replaceFirst("^\\s*", "");
      int nextSpace = rest.indexOf(' ');
      if (nextSpace > 0) {
        rest = rest.substring(nextSpace);
        rest = rest.replaceFirst("^\\s*", "");
        if (StringUtils.startsWithAny(rest, new String[]{"&&", "||"})) {
          rest = rest.substring(2);
          while (rest.matches("\\s.*")) {
            rest = rest.substring(1);
          }
        }
      }

    }
    System.out.println("Final rest: \"" + rest + "\"\n\n");
    return ExpectedType.FIELD;
  }

  public static void main(String[] args) {
    final ArrayList<String> history = new ArrayList<>();
    new QuerySuggestionSource(history).getExpectedType("level>INFO && ");
    new QuerySuggestionSource(history).getExpectedType("level>INFO && message ~= ala");
    new QuerySuggestionSource(history).getExpectedType("level>INFO && message ~=");
  }
}
