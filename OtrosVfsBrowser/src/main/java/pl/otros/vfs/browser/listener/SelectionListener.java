package pl.otros.vfs.browser.listener;

import org.apache.commons.vfs2.FileObject;

import javax.swing.*;

public interface SelectionListener {
  JComponent getView();
  void selectedItem(FileObject... fileObject );
  void selectedContentPart(FileObject fileObject, byte[] sample);
  void enteredDir(FileObject dir);
}
