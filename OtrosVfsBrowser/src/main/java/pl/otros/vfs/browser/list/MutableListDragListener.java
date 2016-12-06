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
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;

public class MutableListDragListener implements DragSourceListener, DragGestureListener {
  private JList list;

  private DragSource ds = new DragSource();

  public MutableListDragListener(final JList list) {
    this.list = list;
    ds.createDefaultDragGestureRecognizer(list,
        DnDConstants.ACTION_MOVE, this);

  }

  public void dragGestureRecognized(final DragGestureEvent dge) {
    final StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
    ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
  }

  public void dragEnter(final DragSourceDragEvent dsde) {
  }

  public void dragExit(final DragSourceEvent dse) {
  }

  public void dragOver(final DragSourceDragEvent dsde) {
  }

  public void dragDropEnd(final DragSourceDropEvent dsde) {

  }

  public void dropActionChanged(final DragSourceDragEvent dsde) {
  }
}