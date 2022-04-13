package pl.otros.logview.store.async;

public class LogDataFilter {
  private final String string;

  public LogDataFilter(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }
}
