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
package pl.otros.logview.gui.util;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;

public class DirectoryRestrictedFileSystemView extends FileSystemView {

  private final File[] rootDirectories;

  public DirectoryRestrictedFileSystemView(File rootDirectory) {
    this.rootDirectories = new File[] { rootDirectory };
  }

  DirectoryRestrictedFileSystemView(File[] rootDirectories) {
    this.rootDirectories = rootDirectories;
  }

  @Override
  public File createNewFolder(File containingDir) throws IOException {
    throw new UnsupportedOperationException("Unable to create directory");
  }

  @Override
  public File[] getRoots() {
    return rootDirectories;
  }

  @Override
  public boolean isRoot(File file) {
    for (File root : rootDirectories) {
      if (root.equals(file)) {
        return true;
      }
    }
    return false;
  }

}
