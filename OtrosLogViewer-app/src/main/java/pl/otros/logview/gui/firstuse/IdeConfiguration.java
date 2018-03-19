package pl.otros.logview.gui.firstuse;

public class IdeConfiguration {
  private final String ideHost;
  private final int idePort;

  IdeConfiguration(String ideHost, int idePort) {
    this.ideHost = ideHost;
    this.idePort = idePort;
  }

  public String getIdeHost() {
    return ideHost;
  }

  public int getIdePort() {
    return idePort;
  }

  @Override
  public String toString() {
    return "IdeConfiguration{" +
      "ideHost='" + ideHost + '\'' +
      ", idePort=" + idePort +
      '}';
  }
}
