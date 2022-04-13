package pl.otros.logview.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DateUtil {

  private static final int SECOND = 1000;
  private static final int MINUTE = 60 * SECOND;
  private static final int HOUR = 60 * MINUTE;

  public Collection<String> allDateFormats() {
    final ArrayList<String> list = new ArrayList<>(dateFormatsInAllLocales());
    list.addAll(getPredefinedDateFormats());
    return list;
  }

  public Set<String> dateFormatsInAllLocales() {
    final Locale[] availableLocales = Locale.getAvailableLocales();
    return Arrays.asList(availableLocales).stream()
      .map(l -> DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, l))
      .map(l -> (SimpleDateFormat) l)
      .map(SimpleDateFormat::toPattern)
      .sorted()
      .collect(Collectors.toSet());
  }

  public Collection<String> getPredefinedDateFormats() {
    return Arrays.asList(
      "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ",
      "yyyy-MM-dd HH:mm:ss.SSSZZZZ",
      "yyyy-MM-dd HH:mm:ss,SSSZZZZ",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss,SSS",
      "yyyy-MM-dd HH:mm:ss"
    );
  }

  public Set<String> matchingDateFormat(Collection<String> definedDateFormats, Collection<String> dates) {
    return definedDateFormats.stream()
      .filter(df ->
        dates
          .stream()
          .anyMatch(date -> canParseDate(df, date)))
      .collect(Collectors.toSet());
  }


  private boolean canParseDate(String dateFormat, String formattedDate) {
    try {
      new SimpleDateFormat(dateFormat).parse(formattedDate);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }

  /**
   * Format time duration to descriptive form like 1,3s, 5h, 2h 3m
   *
   * @param deltaInMillis duration in milliseconds
   * @return time duration in descriptive form
   */
  public static String formatDelta(long deltaInMillis) {
    StringBuilder sb = new StringBuilder();
    if (deltaInMillis < 0) {
      sb.append("-");
    }
    final long abs = Math.abs(deltaInMillis);
    final long millis = abs % SECOND;
    final long seconds = (abs % MINUTE) / SECOND;
    final long minutes = (abs % HOUR) / MINUTE;
    final long hours = (long) Math.floor(((double) abs) / HOUR);
    if (hours > 3) {
      sb.append(hours).append("h");
    } else if (hours > 0) {
      sb.append(hours).append("h");
      if (minutes > 0) {
        sb.append(" ").append(minutes).append("m");
      }
    } else if (minutes > 4) {
      sb.append(minutes).append("m");
    } else if (minutes > 0) {
      sb.append(minutes).append("m");
      if (seconds > 0) {
        sb.append(" ").append(seconds).append("s");
      }
    } else if (seconds > 3) {
      sb.append(seconds).append("s");
    } else if (seconds > 0) {
      sb.append(seconds);
      if (millis != 0) {
        final String s = Long.toString(Math.round((double) millis / 100));
        sb.append(",").append(s);
      }
      sb.append("s");
    } else {
      sb.append(millis).append("ms");
    }
    return sb.toString();
  }


}
