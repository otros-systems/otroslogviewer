package pl.otros.logview.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DateParserTest {


  public static final long NUMBER_OF_DATES_PER_RUN = 100000000L;
  public static final long INCREMENT_PER_DATE = 831L;

  private List<Long> differences = new ArrayList<>();

  @Test(dataProvider = "millisecondsPatterns")
  public void testPatternParser(String pattern) throws ParseException {
    FastDateParser fastDateParser = new FastDateParser(pattern, Locale.ENGLISH);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);

    List<String> dates = createDates(pattern);

    for (String date : dates) {
      long time = fastDateParser.parseMillis(date);
      long expectedTime = simpleDateFormat.parse(date).getTime();
      Assert.assertEquals(time, expectedTime, "The patter " + pattern + " is not working for date " + date);
    }


  }

  @Test(dataProvider = "millisecondsPatterns", enabled = true)
  public void comparePerformance(String pattern) throws ParseException {
    FastDateParser fastDateParser = new FastDateParser(pattern, Locale.ENGLISH);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
    List<String> dates = createDates(pattern);

    long start = System.nanoTime();
    for (String date : dates) {
      Assert.assertTrue(fastDateParser.parseMillis(date) > Long.MIN_VALUE);
    }
    long tookFastParser = System.nanoTime() - start;

    start = System.nanoTime();
    for (String date : dates) {
      Assert.assertTrue(simpleDateFormat.parse(date).getTime() > Long.MIN_VALUE);
    }
    long tookSimpleParser = System.nanoTime() - start;

    differences.add(tookSimpleParser - tookFastParser);
    if (differences.size() == 100) {
      differences.stream()
        .mapToDouble(l -> (double) l)
        .map(d -> d / 10000000d)
        .average()
        .ifPresent(d -> System.out.printf("FastParser is in average %.3fms faster%n", d));
    }

//    if (tookFastParser < tookSimpleParser) {
//      System.out.printf("FastParser is %.3fms faster%n", ((double) tookSimpleParser - tookFastParser) / 10000000L);
//    } else {
//      System.out.printf("FastParser is %.3fms slower%n", ((double) tookFastParser - tookSimpleParser) / 10000000L);
//    }
  }

  @DataProvider(name = "millisecondsPatterns")
  public Object[] millisecondsPatterns() {
    return new String[]{
      "yyyy-MM-dd HH:mm:ss,SSS",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy/MM/dd HH:mm:ss,SSS",
      "yyyy/MM/dd HH:mm:ss.SSS",
      "dd.MM.yyyy HH:mm:ss,SSS",
      "dd.MM.yyyy HH:mm:ss.SSS",
      "dd-MM-yyyy HH:mm:ss,SSS",
      "yyyyMMddHHmmssSSS",
      "yyyy-MM-dd'T'HH:mm:ss.SSS",
      "HH:mm:ss,SSS",
      "HH:mm:ss.SSS",
      "yyyy-MM-dd'T'HH:mm:ss,SSS",
      "HH:mm:ss SSS",
      "yyyy-MM-dd HH:mm:ss SSS",
      "dd/MM/yyyy HH:mm:ss SSS",
      "MM/dd/yyyy HH:mm:ss SSS",
      "yyyy/MM/dd HH:mm:ss SSS",
      "dd.MM.yyyy HH:mm:ss SSS",
      "yyyyMMdd HHmmss SSS",
      "yyyy-MM-dd'T'HH:mm:ss.SSS",
      "yyyy-MM-dd't'HH:mm:ss.SSS",
      "dd-MM-yyyy'T'HH:mm:ss.SSS",
      "dd.MM.yyyy'T'HH:mm:ss.SSS",
      "MM/dd/yyyy'T'HH:mm:ss.SSS",
      "mm:ss,SSS",
      "mm:ss.SSS",
      "ss,SSS",
      "ss.SSS",
      "yyyy MM dd HH mm ss SSS",
      "dd MM yyyy HH mm ss SSS",
      "MM dd yyyy HH mm ss SSS",
      "yyyy-MM-dd HH:mm:ss.SSSSSS",
      "yyyy-MM-dd HH:mm:ss.SSSSS",
      "yyyy-MM-dd HH:mm:ss.SSSS",
      "yyyy-MM-dd HH:mm:ss,SSSSSS",
      "yyyy-MM-dd HH:mm:ss,SSSSS",
      "yyyy-MM-dd HH:mm:ss,SSSS",
      "yyyy-MM-dd HH:mm:ss,SSS",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd'T'HH:mm:ss.SSS",
      "yyyyMMddHHmmssSSS",
      "dd.MM.yyyy HH:mm:ss,SSS",
      "dd/MM/yyyy HH:mm:ss,SSS",
      "MM/dd/yyyy HH:mm:ss,SSS",
      "MMM dd HH:mm:ss,SSS",
      "MMM dd HH:mm:ss.SSS",
      "dd MMM HH:mm:ss,SSS",
      "dd MMM HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss,SSS",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "dd/MM/yyyy HH:mm:ss",
      "MM/dd/yyyy HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss",
      "dd MMM yyyy HH:mm:ss",
      "yyyy-MM-dd",
      "dd.MM.yyyy",
      "MM/dd/yyyy",
      "yyyy/MM/dd",
      "HH:mm:ss",
      "dd/MMM/yyyy:HH:mm:ss",
      "MMM dd, yyyy HH:mm:ss",
      "yyyy-MM-dd'T'HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss.SS",
      "yyyy-MM-dd HH:mm:ss.S",
      "yyyy-MM-dd HH:mm:ss,SS",
      "yyyy-MM-dd HH:mm:ss,S",
      "dd-MMM-yyyy HH:mm:ss",
      "dd.MMM.yyyy HH:mm:ss",
      "MM-dd-yyyy HH:mm:ss",
      "MM.dd.yyyy HH:mm:ss",
      "yyyy.MM.dd HH:mm:ss",
      "dd/MM/yy HH:mm:ss",
      "MM/dd/yy HH:mm:ss",
      "yy/MM/dd HH:mm:ss",
      "dd-MM-yy HH:mm:ss",
      "yy-MM-dd HH:mm:ss",
      "dd.MM.yy HH:mm:ss",
      "yyyyMMddHHmmss",
      "yyyy-MM-dd HH:mm",
      "dd/MM/yyyy HH:mm",
      "MM/dd/yyyy HH:mm",
      "yyyy/MM/dd HH:mm",
      "dd.MM.yyyy HH:mm",
      "HH:mm",
      "yyyy-MM-dd HH:mm:ss,SS",
      "yyyy-MM-dd HH:mm:ss,S",
      "yyyy-MM-dd HH:mm:ss.SS",
      "yyyy-MM-dd HH:mm:ss.S",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd'T'HH:mm:ss",
      "yyyyMMddHHmmss",
      "dd.MM.yyyy HH:mm:ss",
      "dd/MM/yyyy HH:mm:ss",
      "MM/dd/yyyy HH:mm:ss",
      "MMM dd HH:mm:ss",
      "dd MMM HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy/MM/dd HH:mm:ss",
      "dd.MM.yyyy HH:mm:ss",
      "yyyyMMdd HHmmss"
    };
  }


  private List<String> createDates(String pattern) throws ParseException {
    List<String> dates = new ArrayList<>();
    long startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.ENGLISH).parse("2026-07-24 01:00:00,000").getTime();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
    for (long i = startTime; i < startTime + NUMBER_OF_DATES_PER_RUN; i += INCREMENT_PER_DATE) {
      dates.add(simpleDateFormat.format(i));
    }
    return dates;
  }
}
