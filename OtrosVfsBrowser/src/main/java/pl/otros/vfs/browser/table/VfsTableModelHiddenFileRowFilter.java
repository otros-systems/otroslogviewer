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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import javax.swing.*;

public class VfsTableModelHiddenFileRowFilter extends RowFilter<VfsTableModel, Integer> {
  private boolean showHidden;

  public VfsTableModelHiddenFileRowFilter(boolean showHidden) {
    this.showHidden = showHidden;
  }

  @Override
  public boolean include(Entry<? extends VfsTableModel, ? extends Integer> entry) {
    Integer identifier = entry.getIdentifier();
    FileObject fileObject = entry.getModel().get(identifier);
    return checkIfInclude(fileObject);
  }

  protected boolean checkIfInclude(FileObject fileObject) {
    if (!showHidden) {
      try {
        if (fileObject.getName().getBaseName().startsWith(".") || fileObject.isHidden()) {
          return false;
        }
      } catch (FileSystemException e) {

      }
    }
    return true;
  }
}
