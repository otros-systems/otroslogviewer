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


import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import static pl.otros.logview.api.io.Utils.checkIfIsGzipped;
import static pl.otros.logview.api.io.Utils.closeQuietly;
import static pl.otros.logview.api.io.Utils.ungzip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.zip.GZIPInputStream;

public final class LoadingInfo implements AutoCloseable {

  private final String friendlyUrl;
  private final FileObject fileObject;
  private ObservableInputStreamImpl obserableInputStreamImpl;
  private InputStream contentInputStream;
  private final boolean tailing;
  private final boolean gziped;
  private final byte[] inputStreamBufferedStart;
  private long lastFileSize = 0;

  public LoadingInfo(FileObject fileObject) throws IOException {
    this(fileObject, false);
  }

  public LoadingInfo(FileObject fileObject, boolean tailing) throws IOException {
    this.fileObject = fileObject;
    this.friendlyUrl = fileObject.getName().getFriendlyURI();

    final FileContent content = fileObject.getContent();
    InputStream httpInputStream = content.getInputStream();
    byte[] buff = Utils.loadProbe(httpInputStream, 10000);

    this.gziped = checkIfIsGzipped(buff, buff.length);

    ByteArrayInputStream bin = new ByteArrayInputStream(buff);
    SequenceInputStream sequenceInputStream = new SequenceInputStream(bin, httpInputStream);
    ObservableInputStreamImpl observableInputStreamImpl = new ObservableInputStreamImpl(sequenceInputStream);

    if (isGziped()) {
      this.contentInputStream = new GZIPInputStream(observableInputStreamImpl);
      this.inputStreamBufferedStart = ungzip(buff);
    } else {
      this.contentInputStream = observableInputStreamImpl;
      this.inputStreamBufferedStart = buff;
    }
    this.obserableInputStreamImpl = observableInputStreamImpl;

    this.tailing = tailing;
    if (fileObject.getType().hasContent()) {
      setLastFileSize(content.getSize());
    }
  }

  public InputStream getContentInputStream() {
    return contentInputStream;
  }

  public boolean isTailing() {
    return tailing;
  }

  public boolean isGziped() {
    return gziped;
  }

  public String getFriendlyUrl() {
    return friendlyUrl;
  }

  public ObservableInputStreamImpl getObserableInputStreamImpl() {
    return obserableInputStreamImpl;
  }

  public FileObject getFileObject() {
    return fileObject;
  }

  public long getLastFileSize() {
    return lastFileSize;
  }

  public void setLastFileSize(long lastFileSize) {
    this.lastFileSize = lastFileSize;
  }

  public byte[] getInputStreamBufferedStart() {
    return inputStreamBufferedStart;
  }

  public void reload(long position) throws IOException {
    fileObject.refresh();
    long currentSize = fileObject.getContent().getSize();
    obserableInputStreamImpl.close();
    setLastFileSize(currentSize);
    if (gziped) {
      obserableInputStreamImpl = new ObservableInputStreamImpl(fileObject.getContent().getInputStream());
      contentInputStream = new GZIPInputStream(obserableInputStreamImpl);
    } else {
      RandomAccessContent randomAccessContent = fileObject.getContent().getRandomAccessContent(RandomAccessMode.READ);
      randomAccessContent.seek(position);
      obserableInputStreamImpl = new ObservableInputStreamImpl(randomAccessContent.getInputStream(), 0);
      contentInputStream = obserableInputStreamImpl;
    }
  }

  public void reloadIfFileSizeChanged() throws IOException {
    fileObject.refresh();
    long lastFileSize = getLastFileSize();
    long currentSize = fileObject.getContent().getSize();
    if (currentSize > lastFileSize) {
      obserableInputStreamImpl.close();

      RandomAccessContent randomAccessContent = fileObject.getContent().getRandomAccessContent(RandomAccessMode.READ);
      randomAccessContent.seek(lastFileSize);
      setLastFileSize(currentSize);
      obserableInputStreamImpl = new ObservableInputStreamImpl(randomAccessContent.getInputStream(), lastFileSize);
      if (gziped) {
        contentInputStream = new GZIPInputStream(obserableInputStreamImpl);
      } else {
        contentInputStream = obserableInputStreamImpl;
      }
    } else if (currentSize < lastFileSize) {
      obserableInputStreamImpl.close();
      InputStream inputStream = fileObject.getContent().getInputStream();
      obserableInputStreamImpl = new ObservableInputStreamImpl(inputStream, 0);
      if (isGziped()) {
        contentInputStream = new GZIPInputStream(obserableInputStreamImpl);
      } else {
        contentInputStream = obserableInputStreamImpl;
      }
      setLastFileSize(fileObject.getContent().getSize());
    }
  }

  @Override
  public void close() {
    closeQuietly(fileObject);
  }
}
