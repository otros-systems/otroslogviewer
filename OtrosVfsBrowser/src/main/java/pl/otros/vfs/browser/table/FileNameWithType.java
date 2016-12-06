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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;

public class FileNameWithType {

  private FileName fileName;
  private FileType fileType;

  public FileNameWithType(FileName fileName, FileType fileType) {
    super();
    this.fileName = fileName;
    this.fileType = fileType;
  }

  public FileName getFileName() {
    return fileName;
  }

  public void setFileName(FileName fileName) {
    this.fileName = fileName;
  }

  public FileType getFileType() {
    return fileType;
  }

  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

}
