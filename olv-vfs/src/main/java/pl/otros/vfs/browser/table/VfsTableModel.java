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

import pl.otros.vfs.browser.util.VFSUtils;
import pl.otros.vfs.browser.i18n.Messages;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.Date;

public class VfsTableModel extends AbstractTableModel {

  public static final int COLUMN_NAME = 0;
  protected static final int COLUMN_SIZE = 1;
  protected static final int COLUMN_TYPE = 2;
  protected static final int COLUMN_LAST_MOD_DATE = 3;
  private static final String[] COLUMN_NAMES = new String[]{
      Messages.getMessage("model.name"),
      Messages.getMessage("model.size"),
      Messages.getMessage("model.type"),
      Messages.getMessage("model.dateLastMod")
  };
  private static final Logger LOGGER = LoggerFactory.getLogger(VfsTableModel.class);


  private FileObject[] fileObjects = new FileObject[0];
  private FileObjectComparator fileObjectComparator = new FileObjectComparator();

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public int getRowCount() {
    return fileObjects.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    FileObject fileObject = fileObjects[rowIndex];
    boolean isFile = false;
    try {
      isFile = FileType.FILE.equals(fileObject.getType());
    } catch (FileSystemException e1) {
      LOGGER.warn("Can't check file type " + fileObject.getName().getBaseName(), e1);
    }
    if (columnIndex == COLUMN_NAME) {
      try {
        return new FileNameWithType(fileObject.getName(), fileObject.getType());
      } catch (FileSystemException e) {
        return new FileNameWithType(fileObject.getName(), null);
      }
    } else if (columnIndex == COLUMN_TYPE) {
      try {
        return fileObject.getType().getName();
      } catch (FileSystemException e) {
        LOGGER.warn("Can't get file type " + fileObject.getName().getBaseName(), e);
        return "?";
      }
    } else if (columnIndex == COLUMN_SIZE) {
      try {
        long size = -1;
        if (isFile) {
          size = fileObject.getContent().getSize();
        }
        return new FileSize(size);
      } catch (FileSystemException e) {
        LOGGER.warn("Can't get size " + fileObject.getName().getBaseName(), e);
        return new FileSize(-1);
      }
    } else if (columnIndex == COLUMN_LAST_MOD_DATE) {
      try {

        long lastModifiedTime = 0;
        if (!VFSUtils.isHttpProtocol(fileObject)) {
          lastModifiedTime = fileObject.getContent().getLastModifiedTime();
        }
        return new Date(lastModifiedTime);
      } catch (FileSystemException e) {
        LOGGER.warn("Can't get last mod date " + fileObject.getName().getBaseName(), e);
        return null;
      }
    }
    return "?";
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == COLUMN_NAME) {
      return FileNameWithType.class;
    } else if (columnIndex == COLUMN_TYPE) {
      return FileType.class;
    } else if (columnIndex == COLUMN_SIZE) {
      return FileSize.class;
    } else if (columnIndex == COLUMN_LAST_MOD_DATE) {
      return Date.class;
    }
    return super.getColumnClass(columnIndex);
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  public void setContent(FileObject... fileObjects) {
    this.fileObjects = fileObjects;
    Arrays.sort(fileObjects, fileObjectComparator);
    fireTableDataChanged();
  }

  public FileObject get(int row) {
    return fileObjects[row];
  }

}
