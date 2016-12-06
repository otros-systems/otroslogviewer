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

import pl.otros.vfs.browser.Icons;
import pl.otros.vfs.browser.ParentFileObject;
import pl.otros.vfs.browser.util.VFSUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class FileNameWithTypeTableCellRenderer extends DefaultTableCellRenderer {

  private Set<String> archivesSuffixes;

  public FileNameWithTypeTableCellRenderer() {
    archivesSuffixes = new HashSet<String>();
    archivesSuffixes.add("zip");
    archivesSuffixes.add("tar");
    archivesSuffixes.add("jar");
    archivesSuffixes.add("tgz");
    archivesSuffixes.add("gz");
    archivesSuffixes.add("tar");
    archivesSuffixes.add("tbz");
    archivesSuffixes.add("tgz");
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    FileNameWithType fileNameWithType = (FileNameWithType) value;
    FileName fileName = fileNameWithType.getFileName();
    label.setText(fileName.getBaseName());
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
    return label;
  }


}
