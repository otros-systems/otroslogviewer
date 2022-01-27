package pl.otros.logview.parser.log4j;

public class LocationInfo {
    final private String file;
    final private String classname;
    final private String method;
    final private String line;

    public static LocationInfo NA_LOCATION_INFO = new LocationInfo("?","?","?","?");

    public LocationInfo(
            final String file,
            final String classname,
            final String method,
            final String line) {
        this.file = file;
        this.classname = classname;
        this.method = method;
        this.line = line;
    }

    public String getFile() {
        return file;
    }

    public String getClassname() {
        return classname;
    }

    public String getMethod() {
        return method;
    }

    public String getLine() {
        return line;
    }
}
