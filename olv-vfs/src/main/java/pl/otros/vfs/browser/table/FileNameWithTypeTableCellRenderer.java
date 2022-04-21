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

package pl.otros.vfs.browser.table;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.otros.vfs.browser.Icons;
import pl.otros.vfs.browser.ParentFileObject;
import pl.otros.vfs.browser.util.VFSUtils;

public class FileNameWithTypeTableCellRenderer extends DefaultTableCellRenderer {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileNameWithTypeTableCellRenderer.class.getName());

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (value != null) {
      FileNameWithType fileNameWithType = (FileNameWithType) value;
      FileName fileName = fileNameWithType.getFileName();
      label.setText(fileName.getBaseName());
      label.setName("VfsBrowser.file." + fileName.getBaseName());
      label.setToolTipText(fileName.getFriendlyURI());

      FileType fileType = fileNameWithType.getFileType();
      Icon icon = null;
      Icons icons = Icons.getInstance();
      if (fileNameWithType.getFileName().getBaseName().equals(ParentFileObject.PARENT_NAME)) {
        icon = icons.getArrowTurn90();
      } else if (FileType.FOLDER.equals(fileType)) {
        icon = icons.getFolderOpen();
      } else if (VFSUtils.isArchive(fileName)) {
        if ("jar".equalsIgnoreCase(fileName.getExtension())) {
          icon = icons.getJarIcon();
        } else {
          icon = icons.getFolderZipper();
        }
      } else if (FileType.FILE.equals(fileType)) {
        icon = icons.getFile();
      } else if (FileType.IMAGINARY.equals(fileType)) {
        icon = icons.getShortCut();
      }
      label.setIcon(icon);
    } else {
      LOGGER.warn("Rendering for null value: {},{}", row, column);
      label.setText("");
      label.setIcon(null);
    }

    return label;
  }


}
