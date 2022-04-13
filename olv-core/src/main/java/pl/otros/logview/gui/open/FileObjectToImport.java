package pl.otros.logview.gui.open;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.ContentProbe;
import pl.otros.logview.gui.session.OpenMode;
import pl.otros.vfs.browser.table.FileSize;

import java.util.logging.Level;

public class FileObjectToImport {
  private final FileObject fileObject;
  private final FileName fileName;
  private final FileSize fileSize;
  private Level level;
  private OpenMode openMode;
  private CanParse canParse;
  private ContentProbe content;
  private PossibleLogImporters possibleLogImporters;

  FileObjectToImport(FileObject fileObject, FileName fileName, FileSize fileSize, Level level, OpenMode openMode, CanParse canParse,PossibleLogImporters possibleLogImporters) {
    this.fileObject = fileObject;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.level = level;
    this.openMode = openMode;
    this.canParse = canParse;
    this.possibleLogImporters = possibleLogImporters;
  }

  PossibleLogImporters getPossibleLogImporters() {
    return possibleLogImporters;
  }

  void setPossibleLogImporters(PossibleLogImporters possibleLogImporters) {
    this.possibleLogImporters = possibleLogImporters;
  }

  public ContentProbe getContent() {
    return content;
  }

  public void setContent(ContentProbe content) {
    this.content = content;
  }

  FileObject getFileObject() {
    return fileObject;
  }

  FileName getFileName() {
    return fileName;
  }

  FileSize getFileSize() {
    return fileSize;
  }

  public Level getLevel() {
    return level;
  }

  OpenMode getOpenMode() {
    return openMode;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  void setOpenMode(OpenMode openMode) {
    this.openMode = openMode;
  }

  CanParse getCanParse() {
    return canParse;
  }

  void setCanParse(CanParse canParse) {
    this.canParse = canParse;
  }
}


