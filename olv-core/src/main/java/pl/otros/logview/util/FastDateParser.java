package pl.otros.logview.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.time.zone.ZoneRules;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class FastDateParser {
  enum FieldType {
    YEAR,
    MONTH,
    DAY,
    HOUR,
    MINUTE,
    SECOND,
    MILLIS;
  }


  record Field(FieldType type, int start, int length) {
  }

  private static final Map<String, Integer> monthWordCache = new HashMap<>();
  private static final Set<Character> NOT_SUPPORTED_SYMBOLS = Set.of(
    'G', 'Y', 'L', 'w', 'W', 'D', 'F', 'E', 'u', 'a', 'k', 'K', 'h', 'z', 'Z', 'X'
  );

  private int currentYear = Year.now().getValue();
  private final String originalPattern;
  private final Locale locale;
  private final ZoneRules rules;


  private Field yearField;
  private Field monthField;
  private Field dayField;

  private Field hourField;
  private Field minuteField;
  private Field secondField;
  private Field millisField;


  private long cachedHourKey = Long.MIN_VALUE;
  private long cachedHourMillis;
  private SimpleDateFormat alternativeDateFormat = null;

  public FastDateParser(String pattern, Locale locale) {
    this.originalPattern = pattern;
    this.locale = locale;
    this.rules = ZoneId.systemDefault().getRules();

    compilePattern(pattern);


    if (!isParsable(pattern)) {
      alternativeDateFormat = new SimpleDateFormat(pattern, locale);
    }
  }

  private boolean isParsable(String pattern) {
    int pos = 0;
    boolean quoteStart = false;
    while (pos < pattern.length()) {
      char ch = pattern.charAt(pos);

      if (ch == '\'' || quoteStart) {
        quoteStart = !quoteStart;
        pos++;
        continue;
      }

      if (NOT_SUPPORTED_SYMBOLS.contains(ch)) {
        return false;
      }
      pos++;
    }
    return true;
  }

  public long parseMillis(String text) throws ParseException {
    if (alternativeDateFormat != null) {
      return alternativeDateFormat.parse(text).getTime();
    }
    try {
      int parsedYear = parse(yearField, text);
      int parsedMonth = parse(monthField, text);
      int parsedDay = parse(dayField, text);
      int parsedHour = parse(hourField, text);

      long hourKey = parsedYear * 1_000_000L
        + parsedMonth * 10_000L
        + parsedDay * 100L
        + parsedHour;

      if (hourKey == cachedHourKey) {
        return cachedHourMillis
          + parseMinuteOffset(text)
          + parseSecondOffset(text)
          + parseMillisOffset(text);
      }

      cachedHourMillis = buildHourBase(parsedYear, parsedMonth, parsedDay, parsedHour);
      cachedHourKey = hourKey;

      return cachedHourMillis
        + parseMinuteOffset(text)
        + parseSecondOffset(text)
        + parseMillisOffset(text);

    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Cannot parse date: " + text + " with patter: " + originalPattern, e);
    }
  }

  private long buildHourBase(int parsedYear, int parsedMonth, int parsedDay, int parsedHour) {

    /*
    For parsing with the abbreviated year pattern ("y" or "yy"),
    SimpleDateFormat must interpret the abbreviated year relative to some century.
    It does this by adjusting dates to be within 80 years before and 20 years after the time the SimpleDateFormat instance is created.
    For example, using a pattern of "MM/dd/yy" and a SimpleDateFormat instance created on Jan 1, 1997,
    the string "01/11/12" would be interpreted as Jan 11, 2012 while the string "05/04/64" would be interpreted as May 4, 1964.
    During parsing, only strings consisting of exactly two digits, as defined by Character.isDigit(char),
    will be parsed into the default century. Any other numeric string, such as a one digit string,
    a three or more digit string, or a two digit string that isn't all digits (for example, "-1"),
    is interpreted literally. So "01/02/3" or "01/02/003" are parsed, using the same pattern,
    as Jan 2, 3 AD. Likewise, "01/02/-3" is parsed as Jan 2, 4 BC.

    See also: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/text/SimpleDateFormat.html
     */
    int year = parsedYear != 0 ? parsedYear : 1970;
    if (year < 100) {
      if (year + 1900 > currentYear - 80) {
        year += 1900;
      } else {
        year += 2000;
      }
    }

    int month = parsedMonth != 0 ? parsedMonth : 1;
    int day = parsedDay != 0 ? parsedDay : 1;

    LocalDateTime ldt = LocalDateTime.of(year, month, day, parsedHour, 0);
    // It is faster then .atZone(zone).toInstant()
    return ldt.toEpochSecond(rules.getOffset(ldt)) * 1000L;

  }


  private long parseMinuteOffset(String text) {
    return minuteField != null ? parse(minuteField, text) * 60_000L : 0L;
  }

  private long parseSecondOffset(String text) {
    return secondField != null ? parse(secondField, text) * 1_000L : 0L;
  }

  private long parseMillisOffset(String text) {
    return millisField != null ? parse(millisField, text) : 0L;
  }


  private int parse(Field field, String text) {

    if (field == null) {
      return 0;
    }

    int fieldLength = field.length();
    if (field.type() == FieldType.MILLIS && fieldLength < 3) {
      fieldLength = field.start() + 3 > text.length() ? text.length() - field.start() : 3;
    }

    if (field.type() == FieldType.MONTH && field.length() > 2) {
      String value = text.substring(field.start(), field.start() + fieldLength);
      return monthWordCache.computeIfAbsent(value, k ->
        Month.from(
            new DateTimeFormatterBuilder()
              .parseCaseInsensitive()
              .appendPattern("M".repeat(field.length()))
              .toFormatter(locale)
              .parse(value))
          .getValue());
    }


    return parseInt(text, field.start(), fieldLength);
  }

  private int parseInt(String text, int start, int len) {
    int result = 0;
    for (int i = 0; i < len; i++) {
      result = result * 10 + (text.charAt(start + i) - '0');
    }
    return result;
  }

  private void compilePattern(String patter) {
    int pos = 0;
    int quotesCount = 0;
    boolean quoteStart = false;

    while (pos < patter.length()) {
      char ch = patter.charAt(pos);

      if (!Character.isLetter(ch) || quoteStart) {
        if (ch == '\'') {
          quotesCount++;
          quoteStart = !quoteStart;
        }
        pos++;
        continue;
      }

      int start = pos;

      while (pos < patter.length()
        && patter.charAt(pos) == ch) {
        pos++;
      }

      int len = pos - start;
      int startInDateString = start - quotesCount;

      switch (ch) {
        case 'y' -> yearField =
          new Field(FieldType.YEAR, startInDateString, len);

        case 'M' -> monthField =
          new Field(FieldType.MONTH, startInDateString, len);

        case 'd' -> dayField =
          new Field(FieldType.DAY, startInDateString, len);

        case 'H' -> hourField =
          new Field(FieldType.HOUR, startInDateString, len);

        case 'm' -> minuteField =
          new Field(FieldType.MINUTE, startInDateString, len);

        case 's' -> secondField =
          new Field(FieldType.SECOND, startInDateString, len);

        case 'S' -> millisField =
          new Field(FieldType.MILLIS, startInDateString, len);
      }
    }
  }
}