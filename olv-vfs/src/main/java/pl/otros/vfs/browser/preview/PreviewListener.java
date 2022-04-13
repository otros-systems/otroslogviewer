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

package pl.otros.vfs.browser.preview;


import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import pl.otros.vfs.browser.VfsBrowser;
import pl.otros.vfs.browser.listener.SelectionListener;
import pl.otros.vfs.browser.preview.PreviewStatus.State;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 */
public class PreviewListener implements ListSelectionListener {
  private static final String KB = "kB";

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PreviewListener.class);

  private Executor executor;

  private VfsBrowser vfsBrowser;

  private PreviewComponent previewComponent;
  private SelectionListener[] selectionListeners;

  private SwingWorker<PreviewStatus, PreviewStatus> worker;

  public PreviewListener(VfsBrowser vfsBrowser, PreviewComponent component, SelectionListener... selectionListeners) {
    this.vfsBrowser = vfsBrowser;
    this.previewComponent = component;
    this.selectionListeners = selectionListeners;
    executor = Executors.newFixedThreadPool(2);
  }

  @Override
  public void valueChanged(ListSelectionEvent listSelectionEvent) {
    if (listSelectionEvent.getValueIsAdjusting()) {
      return;
    }
    boolean previewEnabled = previewComponent.isPreviewEnabled();
    if (!previewEnabled) {
      return;
    }
    FileObject fileObjectToPreview = null;
    for (FileObject fileObject : vfsBrowser.getSelectedFiles()) {
      try {
        if (fileObject.getType().equals(FileType.FILE)) {
          fileObjectToPreview = fileObject;
          break;
        }
      } catch (FileSystemException e) {
        LOGGER.error("Can't resolve file", e);
      }
    }

    if (fileObjectToPreview != null) {
      makePreview(fileObjectToPreview);
    } else {
      clearPreview();
    }
  }

  private void clearPreview() {
    previewComponent.setPreviewStatus(new PreviewStatus(State.NA, 0, 0, "b", "N/A", EMPTY_BYTE_ARRAY));
  }

  private void makePreview(final FileObject fileObjectToPreview) {
    if (worker != null) {
      worker.cancel(false);
    }
    worker = new SwingWorker<PreviewStatus, PreviewStatus>() {
      private int previewLimit = 20 * 1024;
      private String name = fileObjectToPreview.getName().getBaseName();

      @Override
      protected PreviewStatus doInBackground() throws Exception {
        publish(new PreviewStatus(State.NOT_STARTED, 0, 1, KB, name, EMPTY_BYTE_ARRAY));
        for (int i = 0; i < 5; i++) {
          Thread.sleep(100);
          if (isCancelled()) {
            return new PreviewStatus(State.CANCELLED, 0, previewLimit / 1024, KB, name, EMPTY_BYTE_ARRAY);
          }
        }
        ByteArrayOutputStream outputStreamRef = null;
        try {
          try (
              ByteArrayOutputStream outputStream = new ByteArrayOutputStream(previewLimit);
              InputStream inputStream = fileObjectToPreview.getContent().getInputStream()
          ) {
            outputStreamRef = outputStream;
            byte[] buff = new byte[512];
            int read;
            int max = inputStream.available();
            max = max == 0 ? previewLimit : Math.min(max, previewLimit);
            while ((read = inputStream.read(buff)) > 0 && outputStream.size() < previewLimit) {
              if (isCancelled()) {
                return new PreviewStatus(State.CANCELLED, 0, max / 1024, KB, name, EMPTY_BYTE_ARRAY);
              }
              outputStream.write(buff, 0, read);
              publish(new PreviewStatus(State.LOADING, outputStream.size() / 1024, max / 1024, KB, name, outputStream.toByteArray()));
            }
          }
        } catch (Exception e) {
          LOGGER.error("Exception when downloading preview", e);
          return new PreviewStatus(State.ERROR, outputStreamRef.size() / 1024, outputStreamRef.size() / 1024, KB, name, outputStreamRef.toByteArray());
        }

        return new PreviewStatus(State.FINISHED, outputStreamRef.size() / 1024, outputStreamRef.size() / 1024, KB, name, outputStreamRef.toByteArray());
      }


      @Override
      protected void done() {
        try {
          if (!isCancelled()) {
            PreviewStatus previewStatus = get();
            previewComponent.setPreviewStatus(previewStatus);
            Arrays.stream(selectionListeners).forEach(s->s.selectedContentPart(fileObjectToPreview,previewStatus.getContent()));
          }
        } catch (Exception e) {
          LOGGER.error("Exception when getting result of preview downloading", e);
        }
      }


      @Override
      protected void process(List<PreviewStatus> chunks) {
        PreviewStatus previewStatus = chunks.get(chunks.size() - 1);
        previewComponent.setPreviewStatus(previewStatus);
      }
    };
    executor.execute(worker);

  }
}
