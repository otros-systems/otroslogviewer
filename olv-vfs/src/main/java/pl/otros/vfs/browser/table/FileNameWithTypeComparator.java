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

import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Pattern;

import javax.swing.SortOrder;

import org.apache.commons.vfs2.FileType;

import pl.otros.vfs.browser.ParentFileObject;

public class FileNameWithTypeComparator implements Comparator<FileNameWithType> {
  private SortOrder sortOrder = SortOrder.ASCENDING;

  @Override
  public int compare(FileNameWithType o1, FileNameWithType o2) {
    return compareTo(o1, o2);
  }

  public int compareTo(FileNameWithType o1, FileNameWithType o2) {
    Comparator<FileNameWithType> comp = sortOrder == SortOrder.ASCENDING ? COMP : COMP_REVERSED;
    return comp.compare(o1, o2);
  }


  public void setSortOrder(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
  }

  private static final Comparator<FileNameWithType> NULL_FIRST = (o1, o2) -> {
    if (o1 == null || o1.getFileType() == null || o1.getFileName() == null) {
      return -1;
    } else if (o2 == null || o2.getFileType() == null || o2.getFileName() == null) {
      return 1;
    } else {
      return 0;
    }
  };

  private static final Comparator<FileNameWithType> PARENT_FIRST = (o1, o2) -> {
    if (o1.getFileName().getBaseName().equalsIgnoreCase(ParentFileObject.PARENT_NAME)) {
      return -1;
    } else if (o2.getFileName().getBaseName().equalsIgnoreCase(ParentFileObject.PARENT_NAME)) {
      return 1;
    } else {
      return 0;
    }
  };

  private static final Comparator<FileNameWithType> FOLDER_FIRST = (o1, o2) -> {
    // folders first first
    boolean folder1 = FileType.FOLDER.equals(o1.getFileType());
    boolean folder2 = FileType.FOLDER.equals(o2.getFileType());
    if (folder1 & !folder2) {
      return -1;
    } else if (!folder1 & folder2) {
      return 1;
    } else {
      return 0;
    }
  };

  private static final Comparator<FileNameWithType> BY_NAME = (o1, o2) -> {
    return o1.getFileName().compareTo(o2.getFileName());
  };

  private static final Pattern NUMBERS = Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
  private static final Comparator<FileNameWithType> BY_NAME_RESPECTING_NUMBERS = (o1, o2) -> {
    String s1 = o1.getFileName().getURI();
    String s2 = o2.getFileName().getURI();
    // see https://stackoverflow.com/a/48946875
    // Splitting both input strings by NUMBERS patterns
    String[] split1 = NUMBERS.split(s1);
    String[] split2 = NUMBERS.split(s2);
    for (int i = 0; i < Math.min(split1.length, split2.length); i++) {
      char c1 = split1[i].charAt(0);
      char c2 = split2[i].charAt(0);
      int cmp = 0;

      // If both segments start with a digit, sort them numerically using
      // BigInteger to stay safe
      if (c1 >= '0' && c1 <= '9' && c2 >= 0 && c2 <= '9') {
        cmp = new BigInteger(split1[i]).compareTo(new BigInteger(split2[i]));
      }

      // If we haven't sorted numerically before, or if numeric sorting yielded
      // equality (e.g 007 and 7) then sort lexicographically
      if (cmp == 0) {
        cmp = split1[i].compareTo(split2[i]);
      }

      // Abort once some prefix has unequal ordering
      if (cmp != 0) {
        return cmp;
      }
    }

    // If we reach this, then both strings have equally ordered prefixes, but
    // maybe one string is longer than the other (i.e. has more segments)
    return split1.length - split2.length;
  };

  private static final Comparator<FileNameWithType> STABLE = NULL_FIRST.thenComparing(PARENT_FIRST).thenComparing(FOLDER_FIRST);
  private static final Comparator<FileNameWithType> COMP = STABLE.thenComparing(BY_NAME_RESPECTING_NUMBERS);
  private static final Comparator<FileNameWithType> COMP_REVERSED = STABLE.reversed().thenComparing(BY_NAME_RESPECTING_NUMBERS);

}
