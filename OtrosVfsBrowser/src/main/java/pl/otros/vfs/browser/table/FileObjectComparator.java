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
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class FileObjectComparator implements Comparator<FileObject> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileObjectComparator.class);
  private FileNameWithTypeComparator fileNameWithTypeComparator = new FileNameWithTypeComparator();

  @Override
  public int compare(FileObject o1, FileObject o2) {
    if (o1 != null && o2 != null) {
      try {
        return fileNameWithTypeComparator.compare(new FileNameWithType(o1.getName(), o1.getType()), new FileNameWithType(o2.getName(),
            o2.getType()));
      } catch (FileSystemException e) {
        return 0;
      }
    }
    return 0;
  }

  private int compareTypes(FileType type1, FileType type2) {
    if (type1.equals(FileType.FILE) && !type2.equals(FileType.FILE)) {
      return 1;
    } else if (!type1.equals(FileType.FILE) && type2.equals(FileType.FILE)) {
      return -1;
    }
    return 0;
  }


}
