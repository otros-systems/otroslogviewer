package pl.otros.logview.gui.open;

import pl.otros.logview.gui.session.FileToOpen;

import java.util.List;

public class SessionLoadResult {

  private List<FileObjectToImport> successfullyOpened;
  private List<FileToOpen> failedToOpen;
  private String name;

  public SessionLoadResult(String name, List<FileToOpen> failedToOpen, List<FileObjectToImport> successfullyOpened) {
    this.name = name;
    this.successfullyOpened = successfullyOpened;
    this.failedToOpen = failedToOpen;
  }

  public List<FileObjectToImport> getSuccessfullyOpened() {
    return successfullyOpened;
  }

  public List<FileToOpen> getFailedToOpen() {
    return failedToOpen;
  }

  public String getName() {
    return name;
  }
}
