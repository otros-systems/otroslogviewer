package pl.otros.logview.gui.suggestion;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSearchSuggestionSource extends StringSearchSuggestionSource {


  public static final String COUNT_N_M = "^.*\\{(\\d+),(\\d+)$";
  public static final String COUNT_N = "^.*\\{(\\d+)$";
  final String COUNT_N_TO = "^.*\\{(\\d+),$";

  public RegexSearchSuggestionSource(List<String> history) {
    super(history);
  }

  @Override
  public List<SearchSuggestion> getSuggestions(String queryString) {
    ArrayList<SearchSuggestion> result = new ArrayList<>();
    if (queryString.length() > 1) {
      final String last2 = queryString.substring(queryString.length() - 2);
      if (last2.matches("\\\\[dDwWsS]")) {
        result.addAll(getQuantifiers(queryString));
      }
    }
    if (queryString.matches(COUNT_N_M)) {
      final Matcher matcher = Pattern.compile(COUNT_N_M).matcher(queryString);
      final boolean b = matcher.find();
      if (b && matcher.groupCount() == 2) {
        final String msg = String.format("} - %s to %s times", matcher.group(1), matcher.group(2));
        result.add(new SearchSuggestion(msg, queryString + "}"));
      }
    }
    if (queryString.matches(COUNT_N)) {
      final Matcher matcher = Pattern.compile(COUNT_N).matcher(queryString);
      final boolean b = matcher.find();
      if (b && matcher.groupCount() == 1) {
        final String msg = String.format("} - %s times", matcher.group(1));
        result.add(new SearchSuggestion(msg, queryString + "}"));
      }
    }


    if (queryString.matches(COUNT_N_TO)) {
      final Matcher matcher = Pattern.compile(COUNT_N_TO).matcher(queryString);
      final boolean b = matcher.find();
      if (b && matcher.groupCount() == 1) {
        final String msg = String.format("} - %s to n times", matcher.group(1));
        result.add(new SearchSuggestion(msg, queryString + "}"));
      }
    }

    //quantifiers
    if (queryString.matches(".*\\.")) {
      result.addAll(getQuantifiers(queryString));
    }

    if (queryString.matches(".+\\{")) {
      result.add(new SearchSuggestion("{2}    - exactly 2 times", queryString + "2}"));
      result.add(new SearchSuggestion("{2,}   - at least 2 times ", queryString + "2,}"));
      result.add(new SearchSuggestion("{2,4}  - at least 2 but not more than 4 times", queryString + "2,4}"));
    }

    final Matcher matcherFirstQuantification = Pattern.compile(".*\\{([\\d]+)$").matcher(queryString);
    if (matcherFirstQuantification.find()) {
      final String count = matcherFirstQuantification.group(1);
      result.add(new SearchSuggestion(String.format("{%s}    - exactly %s times", count, count), queryString + count + "}"));
      result.add(new SearchSuggestion(String.format("{%s,}    - at least %s times", count, count), queryString + count + ",}"));
    }

    final Matcher matcherComaQuantification = Pattern.compile(".*\\{([\\d]+),$").matcher(queryString);
    if (matcherComaQuantification.find()) {
      final String count = matcherComaQuantification.group(1);
      final int c = Integer.parseInt(count);
      result.add(new SearchSuggestion(String.format("{%s,}    - at least %s times", count, count), queryString + "}"));
      result.add(new SearchSuggestion(String.format("{%s,%d}  - exactly %d times", count, c + 1, c + 1), queryString + (c + 1) + "}"));
    }

    if (queryString.matches(".*\\{\\d+,?\\d*\\}") ) {
      result.add(new SearchSuggestion("? - Reluctant", queryString + "?"));
      result.add(new SearchSuggestion("+  - Possessive", queryString + "+"));
    }

    if (queryString.matches(".*\\[.+\\]")){
      result.addAll(getQuantifiers(queryString));
    }

    //Predefined characters
    if (queryString.endsWith("\\")) {
      result.add(new SearchSuggestion("\\d - digit [0-9]", queryString + "d"));
      result.add(new SearchSuggestion("\\D - not digit [^0-9]", queryString + "D"));
      result.add(new SearchSuggestion("\\w - word character[a-zA-Z_0-9]", queryString + "w"));
      result.add(new SearchSuggestion("\\W - non-word character [^\\w]", queryString + "W"));
      result.add(new SearchSuggestion("\\s - white character", queryString + "s"));
      result.add(new SearchSuggestion("\\S - not white character", queryString + "S"));
    }

//    Character classes
    if (queryString.endsWith("[")) {
      result.add(new SearchSuggestion("[abc]        - a, b, or c (simple class)", queryString + "abc]"));
      result.add(new SearchSuggestion("[^abc]       - Any character except a, b, or c (negation)", queryString + "^abc]"));
      result.add(new SearchSuggestion("[a-zA-Z]     - a through z or A through Z, inclusive (range)", queryString + "a-zA-Z]"));
      result.add(new SearchSuggestion("[a-d[m-p]]   - a through d, or m through p: [a-dm-p] (union)", queryString + "a-dm-p]"));
      result.add(new SearchSuggestion("[a-z&&[def]] - d, e, or f (intersection)", queryString + "a-z&&[def]]"));
      result.add(new SearchSuggestion("[a-z&&[^bc]] - a through z, except for b and c: [ad-z] (subtraction)", queryString + "a-z&&[^bc]]"));
    }

    final List<SearchSuggestion> history = super.historySuggestions(queryString);
    result.addAll(history);
    return result;
  }

  private List<SearchSuggestion> getQuantifiers(String s) {
    ArrayList<SearchSuggestion> result = new ArrayList<>();
    result.add(new SearchSuggestion("*      - zero or more times - greedy ", s + "*"));
    result.add(new SearchSuggestion("?      - once or not at all - greedy", s + "?"));
    result.add(new SearchSuggestion("+      - one or more times - greedy", s + "+"));
    result.add(new SearchSuggestion("{n}    - exactly n times - greedy", s + "{n}"));
    result.add(new SearchSuggestion("{n,}   - at least n times - greedy", s + "{n,}"));
    result.add(new SearchSuggestion("{n,m}  - at least n but not more than m times - greedy", s + "{n,m}"));

    result.add(new SearchSuggestion("*?     - zero or more times - reluctant ", s + "*?"));
    result.add(new SearchSuggestion("??     - once or not at all - reluctant", s + "??"));
    result.add(new SearchSuggestion("+?     - one or more times - reluctant", s + "+?"));
    result.add(new SearchSuggestion("{n}?   - exactly n times - reluctant", s + "{n}?"));
    result.add(new SearchSuggestion("{n,}?  - at least n times - reluctant", s + "{n,}?"));
    result.add(new SearchSuggestion("{n,m}? - at least n but not more than m times - reluctant", s + "{n,m}?"));

    result.add(new SearchSuggestion("*+     - zero or more times - possessive ", s + "*+"));
    result.add(new SearchSuggestion("?+     - once or not at all - possessive", s + "?+"));
    result.add(new SearchSuggestion("++     - one or more times - possessive", s + "++"));
    result.add(new SearchSuggestion("{n}+   - exactly n times - possessive", s + "{n}+"));
    result.add(new SearchSuggestion("{n,}?  - at least n times - possessive", s + "{n,}+"));
    result.add(new SearchSuggestion("{n,m}+ - at least n but not more than m times - possessive", s + "{n,m}+"));
    return result;
  }
}
