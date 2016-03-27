package pl.otros.logview.api.loading;

public class LogLoadingSession {
  private String id;
  private Source source;


  public LogLoadingSession(String id, Source source) {
    this.id = id;
    this.source = source;
  }

  public String getId() {
    return id;
  }


  public Source getSource() {
    return source;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LogLoadingSession that = (LogLoadingSession) o;

    return !(id != null ? !id.equals(that.id) : that.id != null);

  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}