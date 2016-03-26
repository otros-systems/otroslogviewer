package pl.otros.logview.importer.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import org.slf4j.Marker;
import pl.otros.logview.api.model.LogDataBuilder;
import pl.otros.logview.api.model.MarkerColors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LogbackUtil {

  public static LogDataBuilder translate(ILoggingEvent ev) {
    final LogDataBuilder builder = new LogDataBuilder()
      .withLoggerName(ev.getLoggerName())
      .withMessage(ev.getFormattedMessage())
      .withLevel(convertLevel(ev.getLevel()))
      .withProperties(ev.getMDCPropertyMap())
      .withDate(new Date(ev.getTimeStamp()))
      .withThread(ev.getThreadName());
    addMarker(ev.getMarker(), ev.getMDCPropertyMap(), builder);
    addException(ev.getThrowableProxy(), ev.getMessage(), builder);
    addCallerData(ev, builder);
    return builder;
  }

  public static void addCallerData(ILoggingEvent ev, LogDataBuilder builder) {
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

  public static void addException(IThrowableProxy throwableProxy, String message, LogDataBuilder builder) {
    if (throwableProxy != null) {
      StringBuilder sb = new StringBuilder();
      sb.append(message).append("\n");
      sb.append(throwableProxy.getClassName()).append(": ").append(throwableProxy.getMessage()).append("\n");

      for (StackTraceElementProxy stackTraceElementProxy : throwableProxy.getStackTraceElementProxyArray()) {
        sb.append("\t").append(stackTraceElementProxy.getSTEAsString()).append("\n");
      }
      builder.withMessage(sb.toString());
    }
  }

  public static void addMarker(Marker marker, Map<String, String> mdcMap, LogDataBuilder builder) {
    if (marker != null) {
      builder.withMarked(true);
      builder.withMarkerColors(MarkerColors.Aqua);
      HashMap<String, String> mdc = new HashMap<>(mdcMap);
      mdc.put("marker", marker.toString());
      builder.withProperties(mdc);
    }
  }

  public static Level convertLevel(ch.qos.logback.classic.Level level) {

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
