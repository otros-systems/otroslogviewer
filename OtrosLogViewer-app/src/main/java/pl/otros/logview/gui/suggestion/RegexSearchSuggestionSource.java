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
  public List<SearchSuggestion> getSuggestions(String s) {
    ArrayList<SearchSuggestion> result = new ArrayList<>();
    if (s.length() > 1) {
      final String last2 = s.substring(s.length() - 2);
      if (last2.matches("\\\\[dDwWsS]")) {
        result.addAll(getQuantifiers(s));
      }
    }
    if (s.matches(COUNT_N_M)) {
      final Matcher matcher = Pattern.compile(COUNT_N_M).matcher(s);
      final boolean b = matcher.find();
      if (b && matcher.groupCount() == 2) {
        final String msg = String.format("} - %s to %s times", matcher.group(1), matcher.group(2));
        result.add(new SearchSuggestion(msg, s + "}"));
      }
    }
    if (s.matches(COUNT_N)) {
      final Matcher matcher = Pattern.compile(COUNT_N).matcher(s);
      final boolean b = matcher.find();
      if (b && matcher.groupCount() == 1) {
        final String msg = String.format("} - %s times", matcher.group(1));
        result.add(new SearchSuggestion(msg, s + "}"));
      }
    }


    if (s.matches(COUNT_N_TO)) {
      final Matcher matcher = Pattern.compile(COUNT_N_TO).matcher(s);
      final boolean b = matcher.find();
      if (b && matcher.groupCount() == 1) {
        final String msg = String.format("} - %s to n times", matcher.group(1));
        result.add(new SearchSuggestion(msg, s + "}"));
      }
    }

    //quantifiers
    if (s.matches(".*\\.")) {
      result.addAll(getQuantifiers(s));
    }

    if (s.matches(".+\\{")) {
      result.addAll(getQuantifiers(s));
      result.add(new SearchSuggestion("{2}    - exactly 2 times", s + "2"));
      result.add(new SearchSuggestion("{2,}   - at least 2 times ", s + "2,"));
      result.add(new SearchSuggestion("{2,4}  - at least 2 but not more than 4 times", s + "{2,4}"));
    }

    final Matcher matcherFirstQuantification = Pattern.compile(".*\\{([\\d]+)$").matcher(s);
    if (matcherFirstQuantification.find()) {
      final String count = matcherFirstQuantification.group(1);
      result.add(new SearchSuggestion(String.format("{%s}    - exactly %s times", count, count), s + count + "}"));
      result.add(new SearchSuggestion(String.format("{%s,}    - at least %s times", count, count), s + count + ",}"));
    }

    final Matcher matcherComaQuantification = Pattern.compile(".*\\{([\\d]+),$").matcher(s);
    if (matcherComaQuantification.find()) {
      final String count = matcherComaQuantification.group(1);
      final int c = Integer.parseInt(count);
      result.add(new SearchSuggestion(String.format("{%s,}    - at least %s times", count, count), s + "}"));
      result.add(new SearchSuggestion(String.format("{%s,%d}  - exactly %d times", count, c + 1, c + 1), s + (c + 1) + "}"));
    }

    if (s.matches(".*\\{\\d+,?\\d*\\}") ) {
      result.add(new SearchSuggestion("? - Reluctant", s + "?"));
      result.add(new SearchSuggestion("+  - Possessive", s + "+"));
    }

    if (s.matches(".*\\[.+\\]")){
      result.addAll(getQuantifiers(s));
    }

    //Predefined characters
    if (s.endsWith("\\")) {
      result.add(new SearchSuggestion("\\d - digit [0-9]", s + "d"));
      result.add(new SearchSuggestion("\\D - not digit [^0-9]", s + "D"));
      result.add(new SearchSuggestion("\\w - word character[a-zA-Z_0-9]", s + "w"));
      result.add(new SearchSuggestion("\\W - non-word character [^\\w]", s + "W"));
      result.add(new SearchSuggestion("\\s - white character", s + "s"));
      result.add(new SearchSuggestion("\\S - not white character", s + "S"));
    }

//    Character classes
    if (s.endsWith("[")) {
      result.add(new SearchSuggestion("[abc]        - a, b, or c (simple class)", s + "abc]"));
      result.add(new SearchSuggestion("[^abc]       - Any character except a, b, or c (negation)", s + "^abc]"));
      result.add(new SearchSuggestion("[a-zA-Z]     - a through z or A through Z, inclusive (range)", s + "a-zA-Z]"));
      result.add(new SearchSuggestion("[a-d[m-p]]   - a through d, or m through p: [a-dm-p] (union)", s + "a-dm-p]"));
      result.add(new SearchSuggestion("[a-z&&[def]] - d, e, or f (intersection)", s + "a-z&&[def]]"));
      result.add(new SearchSuggestion("[a-z&&[^bc]] - a through z, except for b and c: [ad-z] (subtraction)", s + "a-z&&[^bc]]"));
    }

    final List<SearchSuggestion> history = super.getSuggestions(s);
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
