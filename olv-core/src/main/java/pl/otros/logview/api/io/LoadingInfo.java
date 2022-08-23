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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.RandomAccessContent;
import pl.otros.logview.gui.session.OpenMode;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static org.apache.commons.vfs2.util.RandomAccessMode.READ;
import static pl.otros.logview.api.io.Utils.closeQuietly;
import static pl.otros.logview.gui.session.OpenMode.FROM_START;

public final class LoadingInfo implements AutoCloseable {

  private final String friendlyUrl;
  private final FileObject fileObject;
  private ObservableInputStreamImpl observableInputStreamImpl;
  private InputStream contentInputStream;
  private final boolean tailing;
  private final CompressType compression;
  private final byte[] inputStreamBufferedStart;
  private long lastFileSize = 0;

  private static final byte MAGIC_ZIP_BYTE_1 = 0x50;
  private static final byte MAGIC_ZIP_BYTE_2 = 0x4B;

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
    try(InputStream inputStream = new BufferedInputStream(fileObject.getContent().getInputStream(), 2)) {
      //maximum limit of reset(). Only read 2 bytes
      inputStream.mark(2);
      compression = checkCompressType(inputStream);
      inputStream.reset();

      if (compression == CompressType.GZIP) {
        inputStreamBufferedStart = ungzip(inputStream, 10_000);
      } else if (compression == CompressType.ZIP) {
        inputStreamBufferedStart = unzip(inputStream, 10_000);
      } else if (compression == CompressType.NONE) {
        inputStreamBufferedStart = loadProbe(inputStream, 10_000);
      } else {
        throw new UnsupportedEncodingException("The compression type " + compression + " is unknown.");
      }
    }

    if (openMode == FROM_START || compression.isCompressed()) {
      fileObject.refresh();//Reload file
      observableInputStreamImpl = new ObservableInputStreamImpl(fileObject.getContent().getInputStream());
      contentInputStream = compression.createInputStream(observableInputStreamImpl);
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

  public CompressType getCompression() {
    return compression;
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
      contentInputStream = compression.createInputStream(observableInputStreamImpl);
    } else if (currentSize < lastFileSize) {
      observableInputStreamImpl.close();
      InputStream inputStream = fileObject.getContent().getInputStream();
      observableInputStreamImpl = new ObservableInputStreamImpl(inputStream, 0);
      contentInputStream = compression.createInputStream(observableInputStreamImpl);
      this.lastFileSize = fileObject.getContent().getSize();
    }
  }

  @Override
  public void close() {
    try {
      observableInputStreamImpl.close();
    } catch (IOException e) {
      //TODO: This class doesn't have a logger.
    }
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

  /**
   * Check the first two bytes of a InputStream.
   * If the bytes equals {@link GZIPInputStream#GZIP_MAGIC} the file is
   * compressed by gzip
   * If the bytes equals {@link #MAGIC_ZIP_BYTE_1} and {@link #MAGIC_ZIP_BYTE_2} the file is compressed by ZIP
   */
  private static CompressType checkCompressType(InputStream inputStream) {
    CompressType compressType = CompressType.NONE;
    byte[] header = new byte[2];
    try {
      int length = inputStream.read(header);
      if(length == 2) {

        if (header[0] == MAGIC_ZIP_BYTE_1 && header[1] == MAGIC_ZIP_BYTE_2) {
          compressType = CompressType.ZIP;
        } else if ((header[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
          && (header[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8))) {
          compressType = CompressType.GZIP;
        }
      }
    } catch (IOException e) {
      //No Action
    }
    return compressType;

  }

  /**
   * Unzip with {@link GZIPInputStream} only the number of bytes set in size parameter
   */
  private static byte[] ungzip(InputStream inputStream, int size) throws IOException {
    try (GZIPInputStream in = new GZIPInputStream(inputStream, size); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[size];
      int len;
      if ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      return out.toByteArray();
    }
  }

  /**
   * Unzip with {@link ZipInputStream} only the number of bytes set in size parameter
   */
  private static byte[] unzip(InputStream inputStream, int size) throws IOException {
    try (ZipInputStream in = new ZipInputStream(inputStream); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[size];
      int len;
      if (in.getNextEntry() != null && (len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      return out.toByteArray();
    }
  }

}
