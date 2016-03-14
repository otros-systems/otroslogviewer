/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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
 ******************************************************************************/
package pl.otros.logview.store.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class InputStreamRandomAccessFileAdapter extends InputStream {

  private final RandomAccessFile accessFile;

  public InputStreamRandomAccessFileAdapter(RandomAccessFile accessFile) {
    super();
    this.accessFile = accessFile;
  }

  public void setPosition(long position) throws IOException {
    accessFile.seek(position);
  }

  @Override
  public int read() throws IOException {
    return accessFile.read();
  }

  @Override
  public int read(byte[] b) throws IOException {

    return accessFile.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return accessFile.read(b, off, len);
  }

  @Override
  public int available() throws IOException {
    long available = accessFile.length() - accessFile.getFilePointer();
    return (int) available;
  }

}
