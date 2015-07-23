package pl.otros.logview.accept.query.org.apache.log4j.suggestion;

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
  private final String operatorsPattern = operators.stream().collect(Collectors.joining("|", "(", ")"));
  private List<String> joins = Splitter.on(" ").splitToList("&& ||");
  private List<String> fields = Splitter.on(" ").splitToList("level logger message class method file line thread mark note prop.");
  private final String fieldssPattern = fields.stream().collect(Collectors.joining("|", "(", ")"));

  private Set<String> fieldsSet = new HashSet<String>(fields);
  private List<String> history;

  public QuerySuggestionSource(List<String> history) {
    this.history = history;
  }

  public enum ExpectedType {
    FIELD,
    FIELD_TAIL,
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
      final List<SearchSuggestion> fields = this.fields.stream().map(f -> new SearchSuggestion(f, f + " ")).collect(Collectors.toList());
      final List<SearchSuggestion> h = history.stream().map(x -> new SearchSuggestion(x, x)).collect(Collectors.toList());
      fields.addAll(h);
      return fields;
    }
    ExpectedType expectedType = getExpectedType(s);
    ArrayList<SearchSuggestion> result = new ArrayList<>();
    if (expectedType == ExpectedType.FIELD) {
      final List<SearchSuggestion> collect = fields.stream()
        .map(f -> new SearchSuggestion(f, s + f + " "))
        .collect(Collectors.toList());
      result.addAll(collect);
    }
    if (expectedType == ExpectedType.FIELD_TAIL) {
      final String fieldHead = getLastNotFinishedCondition(s);
      final List<SearchSuggestion> collect = fields.stream()
        .filter(f->f.startsWith(fieldHead))
        .map(f -> new SearchSuggestion(f, s + f.substring(fieldHead.length()) + " "))
        .collect(Collectors.toList());
      result.addAll(collect);
    } else if (expectedType == ExpectedType.OPERATOR) {
      result.addAll(operators.stream().map(o -> new SearchSuggestion(o, s + " " + o + " ")).collect(Collectors.toList()));
    } else if (expectedType == ExpectedType.VALUE_MSG) {
      //TODO get some suggestion from history?, remove this one
      result.add(new SearchSuggestion("\"ala ma kota\"",s + "\"ala ma kota\""));
    } else if (expectedType == ExpectedType.PROPERTY) {
      result.addAll(Collections.singletonList(new SearchSuggestion(")", s + ") ")));
    } else if (expectedType == ExpectedType.VALUE_LEVEL){
      final List<String> levels = Arrays.asList("FINEST FINER FINE CONFIG INFO WARN SEVERE".split("\\s"));
      result.addAll(levels.stream().map(l->new SearchSuggestion(l,s+l)).collect(Collectors.toList()));
    }

    final List<SearchSuggestion> matchingHistory = history
      .stream()
      .filter(x -> x.contains(s))
      .map(x -> new SearchSuggestion(x, x))
      .collect(Collectors.toList());

    result.addAll(matchingHistory);
    return result;
  }

  ExpectedType getExpectedType(String s) {
    if (s.trim().length() == 0) {
      return ExpectedType.FIELD;
    }
    String rest = getLastNotFinishedCondition(s);

    String trimed = rest.trim();
    if (trimed.length() == 0) {
      return ExpectedType.FIELD;
    } else if (fields.stream().filter(x -> !x.equalsIgnoreCase(trimed) && x.startsWith(trimed)).findFirst().isPresent()) {
      return ExpectedType.FIELD_TAIL;
    } else if (rest.matches("date\\s*" + operatorsPattern + "\\s*")) {
      return ExpectedType.VALUE_DATE;
    } else if (rest.matches("level\\s*" + operatorsPattern + "\\s*")) {
      return ExpectedType.VALUE_LEVEL;
    } else if (rest.matches("message\\s*" + operatorsPattern + "\\s*")) {
      return ExpectedType.VALUE_MSG;
    } else if (rest.matches(fieldssPattern)) {
      return ExpectedType.OPERATOR;
    }

    System.out.println("Final rest: \"" + rest + "\"\n\n");


    return ExpectedType.FIELD;
  }

  protected String getLastNotFinishedCondition(String s) {
    //pattern field operator value
    // level == INFO
    // message != exception
    final String fieldOperatorValue = fieldssPattern + "\\s*" + operatorsPattern + "\\s*(\\S+.*)";
    final Pattern pattern = Pattern.compile(fieldOperatorValue);
    System.out.println("Pattern is " + fieldOperatorValue);
    //get last not finished query =>
    // level>INFO && message ~= ala  =>
    // level>INFO && message ~=   => message ~=
    String rest = s;
    while (rest.matches(fieldOperatorValue)) {
      System.out.println("rest is: " + rest);
      final Matcher matcher = pattern.matcher(rest);
      final boolean b = matcher.find();
      for (int i = 0; i <= matcher.groupCount(); i++) {
        System.out.println("Group " + i + "=" + matcher.group(i));
      }
      rest = matcher.group(3);

      rest = trimLeading(rest);


      //rest can be:
      // value && level>INFO
      // 'message with &&' || .....
      // "message with &&" || .....
      if (rest.matches("\\w+")) {
        rest = "";
      } else if (rest.matches("\\w+\\s*(\\S+.*)")) {
        rest = rest.replaceFirst("\\w+\\s*(\\S+.*)", "$1");
      } else if (rest.matches("'.+?'(.*)")) {
        rest = rest.replaceFirst("'.+?'(.*)", "$1");
      } else if (rest.matches("\".+?\"(.*)")) {
        rest = rest.replaceFirst("\".+?\"(.*)", "$1");
      }

      rest = trimLeading(rest);
      boolean operatorFound = StringUtils.startsWithAny(rest, new String[]{"&&", "||"});
      if (operatorFound) {
        rest = rest.substring(2);
        rest = trimLeading(rest);
      }

      System.out.println("QuerySuggestionSource.getLastNotFinishedCondition: rest:" + rest);
    }


    return rest;
  }

  int countParenthesisBalance(String rest) {
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    final char[] chars = rest.toCharArray();
    int balance = 0;
    for (char c : chars) {
      if (c == '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
      } else if (c == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
      } else if (c == '(' && !inSingleQuote && !inDoubleQuote) {
        balance++;
      } else if (c == ')' && !inSingleQuote && !inDoubleQuote) {
        balance--;
      }
    }
    return balance;
  }

  private String trimLeading(String rest) {
    rest = rest.replaceFirst("^\\s*", "");
    return rest;
  }

  public static void main(String[] args) {
    final List<String> history = Arrays.asList("level>INFO", "level<INFO", "message != ASD");
    final QuerySuggestionSource querySuggestionSource = new QuerySuggestionSource(history);
    querySuggestionSource.getExpectedType("level>INFO && ");
    querySuggestionSource.getExpectedType("level>INFO && message ~= ala");
    querySuggestionSource.getExpectedType("level>INFO && message ~=");

    System.out.println("Suggestion for empty query");
    querySuggestionSource.getSuggestions("").stream().forEach(System.out::println);

    System.out.println("\nSuggestion for empty 'leve'");
    querySuggestionSource.getSuggestions("leve").stream().forEach(System.out::println);
  }
}
