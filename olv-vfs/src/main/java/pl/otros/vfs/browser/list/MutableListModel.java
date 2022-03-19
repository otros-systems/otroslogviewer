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

package pl.otros.vfs.browser.list;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class MutableListModel<T> extends AbstractListModel {

  private ArrayList<T> list;

  public MutableListModel() {
    list = new ArrayList<T>();
  }

  @Override
  public int getSize() {
    return list.size();
  }

  @Override
  public T getElementAt(int index) {
    return list.get(index);
  }

  public void add(T favorite) {
    list.add(favorite);
    fireIntervalAdded(this, list.size() - 1, list.size() - 1);
  }

  public void remove(int index) {
    list.remove(index);
    fireIntervalRemoved(this, index, index);
  }

  public void change(int index, T favorite) {
    list.set(index, favorite);
    fireContentsChanged(this, index, index);
  }

  public void move(int from, int to) {
    list.add(to, list.get(from));
    if (to < from) {
      from++;
    }
    list.remove(from);
    fireContentsChanged(this, Math.min(from, to), Math.max(from, to));

  }

  public List<T> getList() {
    return new ArrayList<T>(list);
  }

}
