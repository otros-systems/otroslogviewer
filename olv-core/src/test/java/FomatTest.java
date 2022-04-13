import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FomatTest {
  public static void main(String[] args) throws ParseException {
    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd '|' HH:mm:ss,SSS");
    System.out.println(format.format(new Date()));
    System.out.println(format.parse("2015-02-09 | 20:55:04,067"));
  }
}
