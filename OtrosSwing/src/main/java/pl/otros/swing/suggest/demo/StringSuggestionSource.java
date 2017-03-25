/*
 * Copyright 2014 otros.systems@gmail.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package pl.otros.swing.suggest.demo;

import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: krzyh
 * Date: 2/28/14
 * Time: 9:39 PM
 * To change this template use File | Settings | File Templates.
 */
class StringSuggestionSource implements SuggestionSource<File> {

  @Override
  public List<File> getSuggestions(SuggestionQuery query) {
    String value = query.getValue();
    ArrayList<File> list = new ArrayList<>();
    if (value.length() == 0) {
      getFilesRoot(list);
      return list;
    }

    final File f = new File(value);
    final String fileName = f.getName();

    File parentFile = f.isDirectory() ? f : f.getParentFile();
    if (parentFile != null) {
      File[] list1 = parentFile.listFiles((dir, name) -> name.startsWith(fileName) || f.isDirectory());
      if (list1 != null) {
        Collections.addAll(list, list1);
      }
    }
    Collections.sort(list, (o1, o2) -> {
      if (o1.isDirectory() && o2.isDirectory() || !o1.isDirectory() && !o2.isDirectory()) {
        return o1.getName().compareTo(o2.getName());
      } else if (o1.isDirectory()) {
        return -1;
      } else if (o2.isDirectory()) {
        return 1;
      }
      return 0;
    });
    return list;
  }

  private void getFilesRoot(ArrayList<File> list) {
    File[] files = File.listRoots();
    Collections.addAll(list, files);
  }
}
