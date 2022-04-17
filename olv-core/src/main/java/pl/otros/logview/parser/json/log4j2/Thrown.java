package pl.otros.logview.parser.json.log4j2;

public class Thrown {
  private String message;
  private String name;
  private String stacktrace;

  public Thrown(String message, String name, String stacktrace) {
    this.message = message;
    this.name = name;
    this.stacktrace = stacktrace;
  }

  public String getMessage() {
    return message;
  }

  public String getName() {
    return name;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  @Override
  public String toString() {
    return "Thrown{" +
      "message='" + message + '\'' +
      ", name='" + name + '\'' +
      ", extendedStackTrace=" + stacktrace +
      '}';
  }
}
