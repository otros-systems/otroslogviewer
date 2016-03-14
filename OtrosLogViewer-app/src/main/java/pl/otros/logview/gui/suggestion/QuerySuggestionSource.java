package pl.otros.logview.gui.suggestion;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.accept.query.org.apache.log4j.rule.InFixToPostFix;
import pl.otros.logview.accept.query.org.apache.log4j.rule.Rule;
import pl.otros.logview.accept.query.org.apache.log4j.rule.RuleFactory;
import pl.otros.logview.accept.query.org.apache.log4j.rule.TimestampInequalityRule;
import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuerySuggestionSource implements SuggestionSource<SearchSuggestion> {

  private static final Logger LOGGER = LoggerFactory.getLogger(QuerySuggestionSource.class.getName());

  private List<String> operators = Splitter.on(" ").splitToList("!= == ~= like exists < > <= >=");
  private final String operatorsPattern = operators.stream().collect(Collectors.joining("|", "(", ")"));
  private List<String> joins = Splitter.on(" ").splitToList("&& ||");
  private List<String> fields = Splitter.on(" ").splitToList("level logger message class method file line thread mark note prop. date");
  private final String fieldsPattern = fields.stream().collect(Collectors.joining("|", "(", ")")) + "\\s*";
  private final String levelPattern = "level\\s*" + operatorsPattern + "\\s*\\w*";

  private Set<String> fieldsSet;
  private List<SuggestionQuery> history;

  public QuerySuggestionSource(List<SuggestionQuery> history) {
    this.history = history;
    fieldsSet = new HashSet<>(fields);
  }

  public enum ExpectedType {
    FIELD,
    FIELD_TAIL,
    OPERATOR,
    VALUE_MSG,
    VALUE_LEVEL,
    VALUE_DATE,
    VALUE_MARK,
    PROPERTY,
    CLOSE_PARENTHESIS,
    JOIN
  }

  @Override
  public List<SearchSuggestion> getSuggestions(SuggestionQuery q) {
    String s = q.getValue();
    if (s.trim().length() == 0) {
      final List<SearchSuggestion> fields = this.fields.stream().map(f -> new SearchSuggestion(f, f + " ")).collect(Collectors.toList());
      final List<SearchSuggestion> h = history.stream()
        .map(SuggestionQuery::getValue)
        .map(x -> new SearchSuggestion(x, x)).collect(Collectors.toList());
      fields.addAll(h);
      return fields;
    }
    List<ExpectedType> expectedType = getExpectedType(s);
    ArrayList<SearchSuggestion> result = new ArrayList<>();
    if (expectedType.contains(ExpectedType.FIELD)) {
      final List<SearchSuggestion> collect = fields.stream()
        .map(f -> new SearchSuggestion(f, s + f + " "))
        .collect(Collectors.toList());
      result.addAll(collect);
    }
    final String fieldHead = getLastNotFinishedCondition(s);

    if (expectedType.contains(ExpectedType.FIELD_TAIL)) {
      final List<SearchSuggestion> collect = fields.stream()
        .filter(f -> fieldHead.length() > 0)
        .filter(f -> f.startsWith(fieldHead))
        .map(f -> new SearchSuggestion(f, s + f.substring(fieldHead.length()) + " "))
        .collect(Collectors.toList());
      result.addAll(collect);
    }

    if (expectedType.contains(ExpectedType.OPERATOR)) {
      result.addAll(operators.stream().map(o -> new SearchSuggestion(o, s + " " + o + " ")).collect(Collectors.toList()));
    }

    if (expectedType.contains(ExpectedType.VALUE_MSG)) {
      final Set<String> message = getValuesForFieldFromHistory("message", history);
      result.addAll(message.stream().map(x -> new SearchSuggestion(x, s + x)).collect(Collectors.toList()));
    }

    if (expectedType.contains(ExpectedType.VALUE_DATE)) {
      final Set<String> message = getValuesForFieldFromHistory("date", history);
      result.addAll(message.stream().map(x -> new SearchSuggestion(x, s + x)).collect(Collectors.toList()));
      //'2012-02-22 19:35:43'
      final List<SimpleDateFormat> formats = new ArrayList<>();
      formats.addAll(Arrays.asList(TimestampInequalityRule.DATE_FORMATS));
      formats.addAll(Arrays.asList(TimestampInequalityRule.TIME_FORMATS));
      final Date now = new Date();
      final List<SearchSuggestion> timeDateInfo = formats.stream()
        .map(x ->
          new SearchSuggestion(
            String.format("Date in format %s, i.e.: %s", x.toPattern(), x.format(now)),
            s + "'" + x.format(now) + "' "))
        .collect(Collectors.toList());
      result.addAll(timeDateInfo);
    }

    if (expectedType.contains(ExpectedType.VALUE_MARK)) {
      final MarkerColors[] values = MarkerColors.values();
      final List<MarkerColors> colors = Arrays.asList(values);
      final List<String> mark = getFieldValues("mark", s);
      final List<MarkerColors> suggestions = colors.stream()
        .filter(x -> mark.stream().filter(colorPart -> x.name().startsWith(colorPart)).findAny().isPresent())
        .collect(Collectors.toList());

      suggestions.stream()
        .map(c -> new SearchSuggestion(c.name(), s + c.name() + " "));
    }

    if (expectedType.contains(ExpectedType.PROPERTY)) {
      result.addAll(Collections.singletonList(new SearchSuggestion(")", s + ") ")));
    }

    if (expectedType.contains(ExpectedType.VALUE_LEVEL)) {
      final List<String> levels = Arrays.asList("FINEST FINER FINE CONFIG INFO WARN SEVERE".split("\\s"));
      final List<String> typedLevels = getFieldValues("level", fieldHead);
      final String typedLevel = typedLevels.size() > 0 ? typedLevels.get(0) : "";
      final Stream<String> levelsStream = levels.stream().filter(l -> l.startsWith(typedLevel));
      result.addAll(levelsStream.map(l -> new SearchSuggestion(l, s + l.substring(typedLevel.length()) + " ")).collect(Collectors.toList()));
    }

    if (expectedType.contains(ExpectedType.JOIN)) {
      final String s1 = s.replaceFirst("(.*[^&\\|])([&\\|]\\s*)", "$1");
      result.addAll(joins.stream().map(l -> new SearchSuggestion(l, s1 + l + " ")).collect(Collectors.toList()));
    }

    final List<SearchSuggestion> matchingHistory = history
      .stream()
      .map(SuggestionQuery::getValue)
      .filter(x -> x.contains(s))
      .filter(x -> !x.equals(s))
      .map(x -> new SearchSuggestion(x, x))
      .collect(Collectors.toList());

    result.addAll(matchingHistory);
    return result;
  }

  protected List<ExpectedType> getExpectedType(String s) {
    final ArrayList<ExpectedType> result = new ArrayList<>();
    if (s.trim().length() == 0) {
      result.add(ExpectedType.FIELD);
      return result;
    }
    String rest = getLastNotFinishedCondition(s);

    String trimmed = rest.trim();
    System.out.printf("Checking expected type for \"%s\"%n", trimmed);
    if (trimmed.length() == 0 && s.matches(".*(&&|\\|\\|)\\s*") || "(".equals(s)) {
      result.add(ExpectedType.FIELD);
    } else if (rest.matches(fieldsPattern)) {
      result.add(ExpectedType.OPERATOR);
    } else if (trimmed.length() == 0 && s.matches(".+\\s+[&\\|]?")) {
      result.add(ExpectedType.JOIN);
    } else if (trimmed.length() > 0 && fields.stream().filter(x -> !x.equalsIgnoreCase(trimmed) && x.startsWith(trimmed)).findFirst().isPresent()) {
      result.add(ExpectedType.FIELD_TAIL);
    } else if (rest.matches("date\\s*" + operatorsPattern + "\\s*")) {
      result.add(ExpectedType.VALUE_DATE);
    } else if (trimmed.matches(levelPattern)) {
      result.add(ExpectedType.VALUE_LEVEL);
    } else if (rest.matches("message\\s*" + operatorsPattern + "\\s*")) {
      result.add(ExpectedType.VALUE_MSG);
    } else if (StringUtils.endsWithAny(trimmed, operators.toArray(new String[operators.size()]))) {
      result.add(ExpectedType.FIELD);
    } else if (trimmed.endsWith("(")) {
      result.add(ExpectedType.FIELD);
    }
    return result;
  }

  protected String getLastNotFinishedCondition(String s) {
    if (s.matches("\\s*(&&\\|\\|)?\\s*\\(.*")) {
      final int closingIndex = closingParenthesisPosition(s);
      if (closingIndex > 0) {
        String substring = s.substring(closingIndex + 1);
        if (substring.matches("\\s*(&&\\|\\|)?\\s*(.*)")) {
          substring = substring.replaceFirst("\\s*(&&|\\|\\|)?\\s*(.*)", "$2");
        }
        return getLastNotFinishedCondition(substring);
      }
    }
    //pattern field operator value
    // level == INFO
    // message != exception
    final String fieldOperatorValue = fieldsPattern + "\\s*" + operatorsPattern + "\\s*(\\S+.*)";
    final Pattern pattern = Pattern.compile(fieldOperatorValue);
//    System.out.println("Pattern is " + fieldOperatorValue);
    //get last not finished query =>
    // level>INFO && message ~= ala  =>
    // level>INFO && message ~=   => message ~=
    String rest = s;
    if (rest.matches(fieldOperatorValue)) {
//      System.out.println("rest is: " + rest);
      final Matcher matcher = pattern.matcher(rest);
      final boolean b = matcher.find();
      for (int i = 0; i <= matcher.groupCount(); i++) {
        System.out.printf("Group %d=\"%s\"%n", i, matcher.group(i));
      }

      String group3 = matcher.group(3);
      group3 = trimLeading(group3);
      if (isLastValue(group3)) {
        return rest;
      }
      //rest can be:
      // value && level>INFO
      // 'message with &&' || .....
      // "message with &&" || .....
      String anotherQuery = "";
      if (group3.matches("\\w+")) {
        anotherQuery = "";
      } else if (group3.matches("\\w+\\s*(\\S+.*)")) {
        anotherQuery = group3.replaceFirst("\\w+\\s*(\\S+.*\\s)", "$1");
      } else if (group3.matches("'.+?'(.*)")) {
        anotherQuery = group3.replaceFirst("'.+?'(.*)", "$1");
      } else if (group3.matches("\".+?\"(.*)")) {
        anotherQuery = group3.replaceFirst("\".+?\"(.*)", "$1");
      }
      rest = anotherQuery;

      rest = trimLeading(rest);
      boolean operatorFound = StringUtils.startsWithAny(rest, new String[]{"&&", "||"});
      if (operatorFound) {
        rest = rest.substring(2);
        rest = trimLeading(rest);
      }
      rest = getLastNotFinishedCondition(rest);
    }

    return rest;
  }

  private boolean isLastValue(String s) {
    if (s.matches("[^\"']\\S*")) {
      return true;
    }
    final String[] splitWithSingleQuotes = s.split(",(?=([^\']*\"[^\']*\')*[^\']*$)", -1);
    if (splitWithSingleQuotes.length > 1) {
      return true;
    }
    final String[] splitWithDoubleQuotes = s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

    return splitWithDoubleQuotes.length > 1;
  }

  public String getLastValue(String s) {
    if (s.matches("[^\"']\\S*")) {
      return s.replaceFirst("([^\"']\\S*).*", "$1");
    }
    final String[] splitWithSingleQuotes = s.split(",(?=([^\']*\"[^\']*\')*[^\']*$)", -1);
    if (splitWithSingleQuotes.length > 1) {
      return splitWithSingleQuotes[1];
    }
    final String[] splitWithDoubleQuotes = s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    if (splitWithDoubleQuotes.length > 1) {
      return splitWithDoubleQuotes[1];
    }
    return s;
  }

  protected Set<String> getValuesForFieldFromHistory(String field, List<SuggestionQuery> history) {
    final Stream<String> stringStream = history.stream().map(SuggestionQuery::getValue).filter(x -> x.contains(field));
    final Stream<String> fieldValues = stringStream.flatMap(s -> getFieldValues(field, s).stream());
    return fieldValues.collect(Collectors.toSet());
  }

  protected List<String> getFieldValues(String field, String query) {
    String rest = query;
    int indexOf = rest.indexOf(field);
    final ArrayList<String> result = new ArrayList<>();
    while (indexOf > -1) {
      rest = rest.substring(indexOf + field.length());
      indexOf = rest.indexOf(field, indexOf + 1);
      final String regex = field + "\\s*" + operatorsPattern + "(.+)";
      if (rest.matches(regex)) {
        //TODO it will not work with args in ''
        //should try use this: http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
        String s = rest.replaceFirst(regex, "$1").trim();
        result.add(s.trim());
      }
    }
    return result;
  }

  protected int countParenthesisBalance(String rest) {
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

  private int closingParenthesisPosition(String rest) {
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    final char[] chars = rest.toCharArray();
    int balance = 0;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (c == '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
      } else if (c == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
      } else if (c == '(' && !inSingleQuote && !inDoubleQuote) {
        balance++;
      } else if (c == ')' && !inSingleQuote && !inDoubleQuote) {
        balance--;
        if (balance == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  private String trimLeading(final String stringToTrim) {
    return stringToTrim.replaceFirst("^\\s*", "");
  }

  public static void main(String[] args) {

    "".matches("(level|logger|message|class|method|file|line|thread|mark|note|prop.|date)\\s*\\s*(!=|==|~=|like|exists|<|>|<=|>=)\\s*(\\S+)(.*)");
    final List<SuggestionQuery> history = Arrays.asList("level>INFO", "level<INFO", "message != ASD")
      .stream()
      .map(s -> new SuggestionQuery(s, s.length()))
      .collect(Collectors.toList());
    final QuerySuggestionSource querySuggestionSource = new QuerySuggestionSource(history);
//    querySuggestionSource.getExpectedType("level>INFO && ");
//    querySuggestionSource.getExpectedType("level>INFO && message ~= ala");
//    querySuggestionSource.getExpectedType("level>INFO && message ~=");
//
//    System.out.println("Suggestion for empty query");
//    querySuggestionSource.getSuggestions("").stream().forEach(System.out::println);
//
//    System.out.println("\nSuggestion for empty 'leve'");
//    querySuggestionSource.getSuggestions("leve").stream().forEach(System.out::println);
//
//    System.out.println("\nSuggestion for empty 'date<'");
//    querySuggestionSource.getSuggestions("date<").stream().forEach(System.out::println);
    querySuggestionSource.getLastNotFinishedCondition("(level>INFO || level<ERROR) && message~=boom ");
    final String[] query = new String[]{
//      "date<'15:22' ",
//      "date<'15:22' &",
//      "date<'15:22' |",
//      "date<'15:22' &&",
//      "date<'15:22' && ",
//      "date<'15:22' && level>INFO ",
//      "date<'15:22' && level>INFO |",
//      "date<'15:22' && level>INFO ||",
//      "(date<'15:22' && level>INFO) |",
//      "(date<'15:22' && (level>INFO || level<WARN)) || li",
//      "line>10 && (date<'15:22' && (level>INFO || level<WARN)) || li",
//      "line>10 && (",
//      "line ",
      "line == 1 && level > F",
      "level > F ",
    };
    for (String s : query) {
      System.out.printf("%nSuggestion for empty \"%s\" %n'", s);
      final List<SearchSuggestion> suggestions = querySuggestionSource.getSuggestions(new SuggestionQuery(s, s.length()));
      System.out.print("Will display: ");
      suggestions
        .stream()
        .forEach(x -> System.out.print(x.getToDisplay() + ","));
      System.out.println();
    }

    tokenize("level >= FINE");
  }


  private static void tokenize(String query) {
    RuleFactory factory = RuleFactory.getInstance();
    Stack<Object> stack = new Stack<>();
    InFixToPostFix.CustomTokenizer tokenizer = new InFixToPostFix.CustomTokenizer(query);

    while (tokenizer.hasMoreTokens()) {
      // examine each token
      String token = tokenizer.nextToken();
      if (token.startsWith("'") || token.startsWith("\"")) {
        String quoteChar = token.substring(0, 1);
        token = token.substring(1);
        while (!token.endsWith(quoteChar) && tokenizer.hasMoreTokens()) {
          token = token + " " + tokenizer.nextToken();
        }
        if (token.length() > 0) {
          token = token.substring(0, token.length() - 1);
        }
      } else {
        // if a symbol is found, pop 2 off the stack,
        // evaluate and push the result
        if (factory.isRule(token)) {
          Rule r = factory.getRule(token, stack);
          stack.push(r);
          // null out the token so we don't try to push it below
          token = null;
        }
      }
      // variables or constants are pushed onto the stack
      if (token != null && token.length() > 0) {
        stack.push(token);
      }
    }

  }
}
