package pl.otros.vfs.browser.demo;

import org.apache.commons.vfs2.FileObject;
import pl.otros.vfs.browser.listener.SelectionListener;

import javax.swing.*;
import java.util.Arrays;

class ExampleListener implements SelectionListener {

  private JLabel label = new JLabel("Listener..");


  @Override
  public JComponent getView() {
    return label;
  }

  @Override
  public void selectedItem(FileObject... fileObject) {
    System.out.println("Listener: selectedItem: " + fileObject.length);
    Arrays.stream(fileObject).map(f->f.getName().getBaseName()).forEach(System.out::println);
    label.setText("Listener: selectedItem: " + fileObject.length);
  }

  @Override
  public void selectedContentPart(FileObject fileObject, byte[] sample) {
    System.out.println("Listener: selectedContentPart " + fileObject.getName() + "\n" + new String(sample));
    label.setText("Listener: selectedContentPart " + fileObject.getName());
  }

  @Override
  public void enteredDir(FileObject dir) {
    System.out.println("Listener: enteredDir " + dir.getName());
    label.setText("Listener: enteredDir " + dir.getName());
  }
}
