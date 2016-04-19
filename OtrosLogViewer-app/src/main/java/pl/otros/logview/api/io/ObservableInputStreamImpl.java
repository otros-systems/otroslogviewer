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

import pl.otros.logview.api.Stoppable;

import java.io.IOException;
import java.io.InputStream;

public class ObservableInputStreamImpl extends InputStream implements ObservableStream, Stoppable {

  private final InputStream src;
  private long current = 0;
  private volatile boolean stop = false;

  public ObservableInputStreamImpl(InputStream src) {
    super();
    this.src = src;
  }

  @Override
  public int read() throws IOException {
    current++;
    return src.read();
  }

  public int available() throws IOException {
    return src.available();
  }

  public void close() throws IOException {
    src.close();
  }

  public void mark(int readlimit) {
    src.mark(readlimit);
  }

  public boolean markSupported() {
    return src.markSupported();
  }

  public int read(byte[] b, int off, int len) throws IOException {
    if (stop)
      return -1;
    int read = src.read(b, off, len);
    if (read>0){
      current += read;
    }
    return read;
  }

  public int read(byte[] b) throws IOException {
    if (stop)
      return -1;
    int read = src.read(b);
    if (read>0){
      current += read;
    }
    return read;
  }

  public void reset() throws IOException {
    src.reset();
  }

  public long skip(long n) throws IOException {
    return src.skip(n);
  }

  /*
   * (non-Javadoc)
   * 
   * @see pl.otros.logview.importer.ni.ObservableStream#getCurrentRead()
   */
  public long getCurrentRead() {
    return current;
  }

  /*
   * (non-Javadoc)
   * 
   * @see pl.otros.logview.importer.ni.ObservableStream#getMax()
   */
  public long getMax() throws IOException {
    return current + src.available();
  }

  @Override
  public void stop() {
    stop = true;
    if (src instanceof Stoppable) {
      ((Stoppable) src).stop();
    }
  }

}
