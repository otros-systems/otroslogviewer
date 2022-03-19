/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.api.io;

import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.vfs2.util.RandomAccessMode.READ;
import static pl.otros.logview.api.io.Utils.closeQuietly;
import static pl.otros.logview.gui.session.OpenMode.FROM_START;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.RandomAccessContent;

import pl.otros.logview.gui.session.OpenMode;

public final class LoadingInfo implements AutoCloseable {

  private final String friendlyUrl;
  private final FileObject fileObject;
  private ObservableInputStreamImpl observableInputStreamImpl;
  private InputStream contentInputStream;
  private final boolean tailing;
  private final boolean gzipped;
  private final byte[] inputStreamBufferedStart;
  private long lastFileSize = 0;

  public LoadingInfo(FileObject fileObject) throws IOException {
    this(fileObject, false, FROM_START);
  }

  public LoadingInfo(FileObject fileObject, boolean tailing) throws IOException {
    this(fileObject, tailing, FROM_START);
  }

  public LoadingInfo(FileObject fileObject, boolean tailing, OpenMode openMode) throws IOException {
    this.fileObject = fileObject;
    this.tailing = tailing;
    friendlyUrl = fileObject.getName().getFriendlyURI();

    fileObject.refresh();
    InputStream inputStream = fileObject.getContent().getInputStream();
    byte[] probe = loadProbe(inputStream, 10000);
    gzipped = checkIfIsGzipped(probe, probe.length);
    inputStreamBufferedStart = gzipped ? ungzip(probe) : probe;

    if (openMode == FROM_START || gzipped) {
      PushbackInputStream pushBackInputStream = new PushbackInputStream(inputStream, probe.length + 1); // +1 to support empty files
      pushBackInputStream.unread(probe);
      observableInputStreamImpl = new ObservableInputStreamImpl(pushBackInputStream);
      contentInputStream = gzipped ? new GZIPInputStream(observableInputStreamImpl) : observableInputStreamImpl;
    } else {
      RandomAccessContent randomAccessContent = fileObject.getContent().getRandomAccessContent(READ);
      randomAccessContent.seek(randomAccessContent.length());
      observableInputStreamImpl = new ObservableInputStreamImpl(randomAccessContent.getInputStream());
      contentInputStream = observableInputStreamImpl;
    }

    if (fileObject.getType().hasContent()) {
      lastFileSize = fileObject.getContent().getSize();
    }
  }

  public InputStream getContentInputStream() {
    return contentInputStream;
  }

  public boolean isTailing() {
    return tailing;
  }

  public boolean isGzipped() {
    return gzipped;
  }

  public String getFriendlyUrl() {
    return friendlyUrl;
  }

  public ObservableInputStreamImpl getObservableInputStreamImpl() {
    return observableInputStreamImpl;
  }

  public FileObject getFileObject() {
    return fileObject;
  }

  public void resetLastFileSize() throws IOException {
    this.lastFileSize = fileObject.getContent().getSize();
  }

  public byte[] getInputStreamBufferedStart() {
    return inputStreamBufferedStart;
  }

  public void reloadIfFileSizeChanged() throws IOException {
    fileObject.refresh();
    long lastFileSize = this.lastFileSize;
    long currentSize = fileObject.getContent().getSize();
    if (currentSize > lastFileSize) {
      observableInputStreamImpl.close();

      RandomAccessContent randomAccessContent = fileObject.getContent().getRandomAccessContent(READ);
      randomAccessContent.seek(lastFileSize);
      this.lastFileSize = currentSize;
      observableInputStreamImpl = new ObservableInputStreamImpl(randomAccessContent.getInputStream(), lastFileSize);
      if (gzipped) {
        contentInputStream = new GZIPInputStream(observableInputStreamImpl);
      } else {
        contentInputStream = observableInputStreamImpl;
      }
    } else if (currentSize < lastFileSize) {
      observableInputStreamImpl.close();
      InputStream inputStream = fileObject.getContent().getInputStream();
      observableInputStreamImpl = new ObservableInputStreamImpl(inputStream, 0);
      if (gzipped) {
        contentInputStream = new GZIPInputStream(observableInputStreamImpl);
      } else {
        contentInputStream = observableInputStreamImpl;
      }
      this.lastFileSize = fileObject.getContent().getSize();
    }
  }

  @Override
  public void close() {
    closeQuietly(fileObject);
  }

  private static byte[] loadProbe(InputStream in, int buffSize) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    byte[] buff = new byte[buffSize];
    int read = in.read(buff);
    if (read > 0) {
      bout.write(buff, 0, read);
    }
    return bout.toByteArray();
  }

  private static boolean checkIfIsGzipped(byte[] buffer, int lenght) throws IOException {
    boolean gziped;
    try {
      ByteArrayInputStream bin = new ByteArrayInputStream(buffer, 0, lenght);
      GZIPInputStream gzipInputStream = new GZIPInputStream(bin);
      gzipInputStream.read(new byte[buffer.length], 0, bin.available());
      gziped = true;
    } catch (IOException e) {
      gziped = false;
    }
    return gziped;
  }

  private static byte[] ungzip(byte[] buff) throws IOException {
    try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(buff)); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      copy(in, out);
      return out.toByteArray();
    }
  }
}
