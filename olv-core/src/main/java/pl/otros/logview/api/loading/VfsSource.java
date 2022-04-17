package pl.otros.logview.api.loading;


import static pl.otros.logview.gui.session.OpenMode.FROM_START;

import org.apache.commons.vfs2.FileObject;

import pl.otros.logview.gui.session.OpenMode;

public class VfsSource extends Source {

  private final FileObject fileObject;
  private final OpenMode openMode;

  public VfsSource(FileObject fileObject) {
    this(fileObject, FROM_START);
  }

  public VfsSource(FileObject fileObject, OpenMode openMode) {
    this.fileObject = fileObject;
    this.openMode = openMode;
  }

  public FileObject getFileObject() {
    return fileObject;
  }

  public OpenMode getOpenMode() {
    return openMode;
  }

  @Override
  public String stringForm() {
    return fileObject.toString();
  }



  @Override
  public String toString() {
    return "VfsSource{" +
        "fileObject=" + fileObject +
        ", openMode=" + openMode +
        '}';
  }
}
