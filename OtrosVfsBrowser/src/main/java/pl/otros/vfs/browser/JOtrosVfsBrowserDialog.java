/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.vfs.browser;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import pl.otros.vfs.browser.i18n.Messages;
import pl.otros.vfs.browser.listener.SelectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 */
public class JOtrosVfsBrowserDialog {

  private Dimension size;

  public enum ReturnValue {
    Approve, Cancelled
  }

  private VfsBrowser vfsBrowser;
  private ReturnValue returnValue = ReturnValue.Cancelled;

  public JOtrosVfsBrowserDialog(final String initialPath, SelectionListener... listeners) {
    this(new DataConfiguration(new BaseConfiguration()), initialPath, listeners);
  }

  public JOtrosVfsBrowserDialog( SelectionListener... listeners) {
    this(new DataConfiguration(new BaseConfiguration()), listeners);
  }

  public JOtrosVfsBrowserDialog(Configuration configuration,  SelectionListener... listeners) {
    this(configuration, (String) null, listeners);
  }

  public JOtrosVfsBrowserDialog(Configuration configuration, final String initialPath, SelectionListener... listeners) {
    super();
    vfsBrowser = new VfsBrowser(configuration, initialPath, listeners);
  }


  public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
    vfsBrowser.setMultiSelectionEnabled(multiSelectionEnabled);
  }

  public void setSelectionMode(SelectionMode selectionMode) {
    vfsBrowser.setSelectionMode(selectionMode);
  }

  public SelectionMode getSelectionMode() {
    return vfsBrowser.getSelectionMode();
  }

  public FileObject[] getSelectedFiles() {
    return vfsBrowser.getSelectedFiles();
  }

  public FileObject getSelectedFile() {
    FileObject[] selectedFiles = vfsBrowser.getSelectedFiles();
    FileObject selectedFile = null;
    if (selectedFiles.length > 0) {
      selectedFile = selectedFiles[0];
    }
    return selectedFile;
  }


  public ReturnValue showOpenDialog(Component parent, String title) {
    JDialog dialog = createDialog(parent);
    dialog.setName("VfsBrowserDialog");
    dialog.setTitle(title);
    if (size == null) {
      dialog.pack();
    } else {
      dialog.setSize(size);
    }
    dialog.setVisible(true);
    size = dialog.getSize();
    return returnValue;
  }


  protected JDialog createDialog(Component parent) throws HeadlessException {
    Frame toUse = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
    final JDialog dialog = new JDialog(toUse);

    dialog.getContentPane().add(vfsBrowser);
    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        cancelSelection();
      }
    });
    vfsBrowser.setApproveAction(new AbstractAction(Messages.getMessage("general.openButtonText")) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        returnValue = ReturnValue.Approve;
        dialog.dispose();
      }
    });
    vfsBrowser.setCancelAction(new AbstractAction(Messages.getMessage("general.cancelButtonText")) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        cancelSelection();
        dialog.dispose();
      }
    });
    dialog.setModal(true);
    dialog.invalidate();
    dialog.repaint();
    return dialog;
  }

  private void cancelSelection() {
    returnValue = ReturnValue.Cancelled;
  }

  public static void main(String[] args) throws FileSystemException {
    if (args.length > 1)
      throw new IllegalArgumentException("SYNTAX:  java... "
          + JOtrosVfsBrowserDialog.class.getName() + " [initialPath]");
    JOtrosVfsBrowserDialog jOtrosVfsBrowserDialog =
        new JOtrosVfsBrowserDialog((args.length < 1) ? null : args[0]);
    jOtrosVfsBrowserDialog.setMultiSelectionEnabled(true);
    jOtrosVfsBrowserDialog.vfsBrowser.setSelectionMode(SelectionMode.DIRS_AND_FILES);
    ReturnValue rv
        = jOtrosVfsBrowserDialog.showOpenDialog(null, "title");
    System.out.println(rv);
    FileObject[] selectedFiles = jOtrosVfsBrowserDialog.getSelectedFiles();
    System.out.println("Selected files count " + selectedFiles.length);
    for (FileObject selectedFile : selectedFiles) {
      System.out.println(selectedFile.getType().toString() + ": " + selectedFile.getURL());

    }
    System.exit(0);
  }
}
