package pl.otros.logview.api.loading;


import org.apache.commons.vfs2.FileObject;

public class VfsSource extends Source {

  private final FileObject fileObject;
  private final long position;

  public VfsSource(FileObject fileObject) {
    this(fileObject,0);
  }

  public VfsSource(FileObject fileObject, long position) {
    this.fileObject = fileObject;
    this.position = position;
  }

  public FileObject getFileObject() {
    return fileObject;
  }

  public long getPosition() {
    return position;
  }

  @Override
  public String stringForm() {
    return toString();
  }



  @Override
  public String toString() {
    return "VfsSource{" +
        "fileObject=" + fileObject +
        ", position=" + position +
        '}';
  }
}
