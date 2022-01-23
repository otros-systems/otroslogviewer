package pl.otros.logview.parser.log4j;

import org.apache.logging.log4j.Level;

import java.util.Map;

public class LoggingEvent {

      private final String fqnOfCategoryClass;
      private final String logger;
      private final long timeStamp;
      private final Level level;
      private final Object message;
      private final String threadName;
      private final String[] throwable;
      private final String ndc;
      private final LocationInfo locationInfo;
      private final Map properties;

      public LoggingEvent(final String fqnOfCategoryClass,
                          final String logger,
                          final long timeStamp,
                          final Level level,
                          final Object message,
                          final String threadName,
                          final String[] throwable,
                          final String ndc,
                          final LocationInfo locationInfo,
                          final java.util.Map properties){
            this.fqnOfCategoryClass = fqnOfCategoryClass;
            this.logger = logger;
            this.timeStamp = timeStamp;
            this.level = level;
            this.message = message;
            this.threadName = threadName;
            this.throwable = throwable;
            this.ndc = ndc;
            this.locationInfo = locationInfo;
            this.properties = properties;
      }

      public String getFqnOfCategoryClass() {
            return fqnOfCategoryClass;
      }

      public String getLogger() {
            return logger;
      }

      public long getTimeStamp() {
            return timeStamp;
      }

      public Level getLevel() {
            return level;
      }

      public Object getMessage() {
            return message;
      }

      public String getThreadName() {
            return threadName;
      }

      public String[] getThrowable() {
            return throwable;
      }

      public String getNdc() {
            return ndc;
      }

      public LocationInfo getLocationInfo() {
            return locationInfo;
      }

      public Map getProperties() {
            return properties;
      }
}
