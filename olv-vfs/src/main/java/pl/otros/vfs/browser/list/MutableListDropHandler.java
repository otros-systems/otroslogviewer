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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class MutableListDropHandler extends TransferHandler {
  private JList list;

  public MutableListDropHandler(final JList list) {
    this.list = list;
  }

  public boolean canImport(final TransferHandler.TransferSupport support) {
    if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      return false;
    }
    final JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
    if (dl.getIndex() == -1) {
      return false;
    } else {
      return true;
    }
  }

  public boolean importData(final TransferHandler.TransferSupport support) {
    if (!canImport(support)) {
      return false;
    }

    final Transferable transferable = support.getTransferable();
    String indexString;
    try {
      indexString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
    } catch (final Exception e) {
      e.printStackTrace();
      return false;
    }

    int index = Integer.parseInt(indexString);
    final JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
    final int dropTargetIndex = dl.getIndex();

    final MutableListModel<?> model = (MutableListModel<?>) list.getModel();
    model.move(index, dropTargetIndex);
    return true;
  }
}