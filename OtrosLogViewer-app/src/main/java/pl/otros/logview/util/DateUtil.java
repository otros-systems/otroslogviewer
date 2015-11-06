package pl.otros.logview.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DateUtil {

  public Collection<String> allDateFormats(){
    final ArrayList<String> list = new ArrayList<>(dateFormatsInAllLocales());
    list.addAll(getPredefinedDateFormats());
    return list;
  }

  public Set<String> dateFormatsInAllLocales(){
    final Locale[] availableLocales = Locale.getAvailableLocales();
    return Arrays.asList(availableLocales).stream()
      .map(l -> DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, l))
      .map(l -> (SimpleDateFormat) l)
      .map(SimpleDateFormat::toPattern)
      .sorted()
      .collect(Collectors.toSet());
  }

  public Collection<String> getPredefinedDateFormats(){
    return Arrays.asList(
      "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ",
      "yyyy-MM-dd HH:mm:ss.SSSZZZZ",
      "yyyy-MM-dd HH:mm:ss,SSSZZZZ",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss,SSS",
      "yyyy-MM-dd HH:mm:ss"
    );
  }

  public Set<String> matchingDateFormat(Collection<String> definedDateFormats,Collection<String> dates) {
    return definedDateFormats.stream()
      .filter(df ->
        dates.stream()
          .filter(date -> canParseDate(df, date))
          .findFirst()
          .isPresent())
      .collect(Collectors.toSet());
  }


  private boolean canParseDate(String dateFormat, String formattedDate){
    try {
      new SimpleDateFormat(dateFormat).parse(formattedDate);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }

  public static void main(String[] args) {
    final DateUtil dateUtil = new DateUtil();
    final List<String> dates = Arrays.asList("2015 08 24 20:43:43.082", "2015-08-24T20:23:43.082+0000");
    final Collection<String> allFormats = dateUtil.allDateFormats();
    allFormats.stream().forEach(s -> System.out.println(new SimpleDateFormat(s).format(new Date())));
    final Set<String> strings = dateUtil.matchingDateFormat(allFormats, dates);

    System.out.println("Have " + strings.size() + " matching formats");
  }
}
