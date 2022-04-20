package pl.otros.logview.api.loading;


import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.gui.session.OpenMode;

import static pl.otros.logview.gui.session.OpenMode.FROM_START;

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
    StringBuilder builder = new StringBuilder();
    builder.append("VfsSource{");
    if (fileObject != null && fileObject.getName() != null) {
      builder.append("fileObject=").append(fileObject.getName().getFriendlyURI());
    } else {
      builder.append("fileObject=null");
    }
    builder.append(", openMode=").append(openMode);
    builder.append('}');

    return builder.toString();
  }

}
