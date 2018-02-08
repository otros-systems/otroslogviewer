package pl.otros.logview.gui.session;

import java.util.List;

public class Session {

  private String name;
  private List<FileToOpen> filesToOpen;

  public Session(String name, List<FileToOpen> filesToOpen) {
    this.name = name;
    this.filesToOpen = filesToOpen;
  }

  public List<FileToOpen> getFilesToOpen() {
    return filesToOpen;
  }

  public String getName() {
    return name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Session session = (Session) o;

    if (name != null ? !name.equals(session.name) : session.name != null) return false;
    return filesToOpen != null ? filesToOpen.equals(session.filesToOpen) : session.filesToOpen == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (filesToOpen != null ? filesToOpen.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Session{" +
        "name='" + name + '\'' +
        ", filesToOpen=" + filesToOpen +
        '}';
  }
}
