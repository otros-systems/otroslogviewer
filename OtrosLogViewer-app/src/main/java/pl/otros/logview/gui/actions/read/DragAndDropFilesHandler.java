/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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
 ******************************************************************************/
package pl.otros.logview.gui.actions.read;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.actions.TailLogActionListener;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.logview.importer.LogImporter;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for global drag-and-drop operations.
 * 
 * Currently supports dropping possibly multiple log files that are then opened by autodetection.
 * 
 * @author murat
 */
public class DragAndDropFilesHandler extends TransferHandler {

  private static final String APPLICATION_X_JAVA_URL_DATA_FLAVOR = "application/x-java-url; class=java.net.URL";
  private static final String TEXT_URI_LIST = "text/uri-list; class=java.lang.String; charset=Unicode";

  private static final long serialVersionUID = 3830464109280595888L;

  static final Logger LOGGER = Logger.getLogger(DragAndDropFilesHandler.class.getName());
	private OtrosApplication otrosApplication;


	public DragAndDropFilesHandler(OtrosApplication otrosApplication) {
		this.otrosApplication = otrosApplication;
	}

  @Override
  public boolean canImport(TransferSupport support) {
    if (isText(support) || isListOfFiles(support) || isUriList(support) || isURL(support)) {
      LOGGER.info("Can import support ");
      return true;
    }
    return super.canImport(support);
  }

  private boolean isListOfFiles(TransferSupport support) {
    boolean dataFlavorSupported = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    LOGGER.info("Data transfer is list of files: " + dataFlavorSupported);
    return dataFlavorSupported;
  }

  private boolean isText(TransferSupport support) {
    boolean b = DataFlavor.selectBestTextFlavor(support.getDataFlavors()) != null;
    LOGGER.info("Data transfer is text: " + b);
    return b;
  }

  private boolean isURL(TransferSupport support) {
    DataFlavor[] dataFlavors = support.getDataFlavors();
    boolean isUrl = false;
    for (DataFlavor dataFlavor : dataFlavors) {
      if (dataFlavor.getMimeType().equals(APPLICATION_X_JAVA_URL_DATA_FLAVOR)) {
        isUrl = true;
        break;
      }
    }
    LOGGER.info("Data transfer is list of file: " + isUrl);
    return isUrl;
  }

  private boolean isUriList(TransferSupport support) {
    DataFlavor[] dataFlavors = support.getDataFlavors();
    boolean isUriList = false;
    for (DataFlavor dataFlavor : dataFlavors) {
      if (dataFlavor.getMimeType().equals(TEXT_URI_LIST)) {
        isUriList = true;
        break;
      }
    }
    LOGGER.info("Data transfer is URI list: " + isUriList);
    return isUriList;
  }

  @Override
  public boolean importData(TransferSupport support) {

    if (isURL(support)) {
      LOGGER.finest("Importing URL");
      return importUrl(support);
    }

    if (isUriList(support)) {
      LOGGER.finest("Importing URI list");
      return importUriList(support);
    }

    if (isText(support)) {
      LOGGER.finest("Importing text");
      return importString(support);
    }

    if (isListOfFiles(support)) {
      LOGGER.finest("Importing list of files");
      return importListOfFiles(support);
    }

    return super.importData(support);
  }

  private boolean importListOfFiles(TransferSupport support) {
    try {
      return tryToImportListOfFiles(support);
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Problem dropping files on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of files: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private boolean tryToImportListOfFiles(TransferSupport support) {
    for (File file : getListOfFiles(support)) {
      openLogFile(getFileObjectForLocalFile(file));
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private List<File> getListOfFiles(TransferSupport support) {
    try {
      return (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
    } catch (UnsupportedFlavorException e) {
      throw new RuntimeException("The dropped data is not supported. The unsupported DataFlavors are " + StringUtils.join(support.getDataFlavors(), ","), e);
    } catch (IOException e) {
      throw new RuntimeException("Cannot read the dropped data.", e);
    }
  }

  private boolean importString(TransferSupport support) {
    try {
      tryToImportString(support);
      return true;
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Problem dropping something on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of strings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private boolean importUrl(TransferSupport support) {
    try {
      tryToImportUrl(support);
      return true;
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Problem dropping something on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of strings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Problem dropping something on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of strings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private boolean importUriList(TransferSupport support) {
    try {
      tryToImportUriList(support);
      return true;
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Problem dropping something on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of strings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Problem dropping something on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of strings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void tryToImportString(TransferSupport support) {
    for (FileObject file : getFileObjects(support)) {
      openLogFile(file);
    }
  }

  private void tryToImportUrl(TransferSupport support) throws UnsupportedFlavorException, IOException, ClassNotFoundException {

    URL transferData = (URL) support.getTransferable().getTransferData(new DataFlavor(APPLICATION_X_JAVA_URL_DATA_FLAVOR));

    openLogFile(VFS.getManager().resolveFile(transferData.toString()));

  }

  private void tryToImportUriList(TransferSupport support) throws UnsupportedFlavorException, IOException, ClassNotFoundException {

    String transferData = (String) support.getTransferable().getTransferData(new DataFlavor(TEXT_URI_LIST));
    String[] split = transferData.split("\n");
    for (String string : split) {
      string = string.trim();
      if (StringUtils.isNotBlank(string)) {
        openLogFile(VFS.getManager().resolveFile(string));
      }
    }

  }

  private List<FileObject> getFileObjects(TransferSupport support) {
    List<FileObject> files = new ArrayList<FileObject>();
    for (String uriString : getFileUris(support)) {
      files.add(getFileObjectForLocalFile(getFileForUriString(uriString)));
    }
    return files;
  }

  public List<String> getFileUris(TransferSupport support) {
    BufferedReader reader = null;
    List<String> files = new ArrayList<String>();
    try {
      reader = new BufferedReader(DataFlavor.selectBestTextFlavor(support.getDataFlavors()).getReaderForText(support.getTransferable()));
      String uri = null;
      while ((uri = reader.readLine()) != null) {
        files.add(uri);
      }
    } catch (UnsupportedFlavorException e) {
      throw new RuntimeException("The dropped data is not supported. The unsupported DataFlavors are " + StringUtils.join(support.getDataFlavors(), ","), e);
    } catch (IOException e) {
      throw new RuntimeException("Cannot read the dropped data.", e);
    }
    return files;
  }

  private FileObject getFileObjectForLocalFile(File file) {
    try {
      return VFS.getManager().toFileObject(file);
    } catch (FileSystemException e) {
      throw new RuntimeException("Cannot open file: " + file, e);
    }
  }

  private File getFileForUriString(String uriString) {
    LOGGER.finest(String.format("Creating uri for %s", uriString));
    return new File(URI.create(uriString));
  }

  private void openLogFile(FileObject file) {
		Collection<LogImporter> importers = otrosApplication.getAllPluginables().getLogImportersContainer().getElements();
		LogImporter importer = new DetectOnTheFlyLogImporter(importers);
    TailLogActionListener tailLogActionListener = new TailLogActionListener(otrosApplication, importer);
    tailLogActionListener.openFileObjectInTailMode(file);

    // new LogFileInNewTabOpener(new AutoDetectingImporterProvider(importers), tabbedPane, observer).open(file);
  }

}
