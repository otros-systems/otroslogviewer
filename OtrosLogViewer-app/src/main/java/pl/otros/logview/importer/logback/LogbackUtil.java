package pl.otros.logview.importer.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import org.slf4j.Marker;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

public class LogbackUtil {

  public static LogDataBuilder translate(ILoggingEvent ev){
    final LogDataBuilder builder = new LogDataBuilder()
      .withLoggerName(ev.getLoggerName())
      .withMessage(ev.getMessage())
      .withLevel(convertLevel(ev.getLevel()))
      .withProperties(ev.getMDCPropertyMap())
      .withDate(new Date(ev.getTimeStamp()))
      .withThread(ev.getThreadName());
    addMarker(ev, builder);
    addException(ev, builder);
    addCallerData(ev, builder);

    return builder;
  }

  private static void addCallerData(ILoggingEvent ev, LogDataBuilder builder) {
    if (ev.hasCallerData()) {
      final StackTraceElement[] callerData = ev.getCallerData();
      if (callerData.length > 0) {
        final StackTraceElement stackTraceElement = callerData[0];
        builder.withClass(stackTraceElement.getClassName());
        builder.withMethod(stackTraceElement.getMethodName());
        builder.withLineNumber(Integer.toString(stackTraceElement.getLineNumber()));
      }
    }
  }

  private static void addException(ILoggingEvent ev, LogDataBuilder builder) {
    if (ev.getThrowableProxy()!=null){
      final IThrowableProxy throwableProxy = ev.getThrowableProxy();
      StringBuilder sb= new StringBuilder();
      sb.append(throwableProxy.getCause()).append("\n");
      for (StackTraceElementProxy stackTraceElementProxy : throwableProxy.getStackTraceElementProxyArray()) {
        sb.append("\tat ").append(stackTraceElementProxy.getSTEAsString());
      }
      builder.withMessage(ev.getMessage() +"\n" + sb.toString());
    }
  }

  private static void addMarker(ILoggingEvent ev, LogDataBuilder builder) {
    final Marker marker = ev.getMarker();
    if (marker!=null){
      builder.withMarked(true);
      builder.withMarkerColors(MarkerColors.Aqua);
      HashMap<String,String> mdc = new HashMap<>(ev.getMDCPropertyMap());
      mdc.put("marker", marker.toString());
      builder.withProperties(mdc);
    }
  }

  static Level convertLevel(ch.qos.logback.classic.Level level) {

    if (ch.qos.logback.classic.Level.DEBUG_INT == level.toInt()) {
      return Level.FINE;
    } else if (ch.qos.logback.classic.Level.TRACE_INT == level.toInt()) {
      return Level.FINEST;
    } else if (ch.qos.logback.classic.Level.INFO_INT == level.toInt()) {
      return Level.INFO;
    } else if (ch.qos.logback.classic.Level.WARN_INT == level.toInt()) {
      return Level.WARNING;
    } else if (ch.qos.logback.classic.Level.ERROR_INT == level.toInt()) {
      return Level.SEVERE;
    }
    return Level.INFO;
  }
}
