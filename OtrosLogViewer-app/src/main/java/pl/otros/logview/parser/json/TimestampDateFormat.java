package pl.otros.logview.parser.json;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

class TimestampDateFormat extends DateFormat {

  @Override
  public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    toAppendTo.append(Long.toString(date.getTime()));
    return toAppendTo;

  }

  @Override
  public Date parse(String source, ParsePosition pos) {
    final int start = pos.getIndex();
    final String substring = source.substring(start);
    pos.setIndex(source.length());
    return new Date(Long.parseLong(substring));
  }
}
