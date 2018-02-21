/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.gui.actions.TailLogActionListener;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Responsible for global drag-and-drop operations.
 * <p>
 * Currently supports dropping possibly multiple log files that are then opened by autodetection.
 *
 * @author murat
 */
public class DragAndDropFilesHandler extends TransferHandler {

  private static final String APPLICATION_X_JAVA_URL_DATA_FLAVOR = "application/x-java-url; class=java.net.URL";
  private static final String TEXT_URI_LIST = "text/uri-list; class=java.lang.String; charset=Unicode";

  private static final long serialVersionUID = 3830464109280595888L;

  private static final Logger LOGGER = LoggerFactory.getLogger(DragAndDropFilesHandler.class.getName());
  private final OtrosApplication otrosApplication;


  public DragAndDropFilesHandler(OtrosApplication otrosApplication) {
    this.otrosApplication = otrosApplication;
  }

  @Override
  public boolean canImport(TransferSupport support) {
    LOGGER.info("Checking if can import data from DnD");
    boolean canImport = (isListOfFiles(support) || isUriList(support) || isURL(support));
    LOGGER.info("Can import from DnD " + canImport);
    return canImport;
  }

  private boolean isListOfFiles(TransferSupport support) {
    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
  }

  private boolean isURL(TransferSupport support) {
    return isMimeType(support, APPLICATION_X_JAVA_URL_DATA_FLAVOR);
  }

  private boolean isMimeType(TransferSupport support, String mimeType) {
    try {
      final DataFlavor flavor = new DataFlavor(mimeType);
      return isMimeType(support, flavor);
    } catch (Exception ignored) {
    }
    return false;

  }

  private boolean isMimeType(TransferSupport support, DataFlavor flavor) {
    try {
      if (support.getTransferable().isDataFlavorSupported(flavor)){
        final Object transferData = support.getTransferable().getTransferData(flavor);
        return null != transferData;
      }
    } catch (Exception ignored) {
    }
    return false;
  }

  private boolean isUriList(TransferSupport support) {
    return isMimeType(support, TEXT_URI_LIST);
  }

  @Override
  public boolean importData(TransferSupport support) {
    LOGGER.info("Importing data from DnD");
    if (isURL(support)) {
      LOGGER.trace("Importing URL");
      return importUrl(support);
    }

    if (isListOfFiles(support)) {
      LOGGER.trace("Importing list of files");
      return importListOfFiles(support);
    }

    if (isUriList(support)) {
      LOGGER.trace("Importing URI list");
      return importUriList(support);
    }

    return super.importData(support);
  }

  private boolean importListOfFiles(TransferSupport support) {
    try {
      return tryToImportListOfFiles(support);
    } catch (RuntimeException e) {
      LOGGER.error("Problem dropping files on the GUI: " + e.getMessage(), e);
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

  private boolean importUrl(TransferSupport support) {
    try {
      tryToImportUrl(support);
      return true;
    } catch (Exception e) {
      LOGGER.error("Problem dropping something on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of strings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private boolean importUriList(TransferSupport support) {
    try {
      tryToImportUriList(support);
      return true;
    } catch (Exception e) {
      LOGGER.error("Problem dropping something on the GUI: " + e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "Problem during drag-and-drop of strings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return false;
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

  private FileObject getFileObjectForLocalFile(File file) {
    try {
      return VFS.getManager().toFileObject(file);
    } catch (FileSystemException e) {
      throw new RuntimeException("Cannot open file: " + file, e);
    }
  }

  private void openLogFile(FileObject file) {
    Collection<LogImporter> importers = otrosApplication.getAllPluginables().getLogImportersContainer().getElements();
    LogImporter importer = new DetectOnTheFlyLogImporter(importers);
    TailLogActionListener tailLogActionListener = new TailLogActionListener(otrosApplication, importer);
    tailLogActionListener.openFileObjectInTailMode(file, Utils.getFileObjectShortName(file));
  }

}
