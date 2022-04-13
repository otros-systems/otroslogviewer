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
package net.sf.vfsjfilechooser.accessories.bookmarks;

/**
 * A specialisation of {@link TitledURLEntry} that can be used to store FTP connection specific
 * information.
 *
 * @author Alex Arana
 */
public class FTPURLEntry extends TitledURLEntry {
  /**
   * Passive FTP option.
   */
  private boolean passiveFtp;

  /**
   * New FTPURLEntry with title, url and passiveFtp option.
   *
   * @param title
   * @param url
   * @param passiveFtp
   */
  public FTPURLEntry(String title, String url, boolean passiveFtp) {
    super(title, url);
    this.passiveFtp = passiveFtp;
  }

  /**
   * Copy constructor (from TitledURLEntry). Sets the passiveFtp mode flag to <code>false</code>
   * by default.
   *
   * @param entry to read most initial properties from
   */
  FTPURLEntry(TitledURLEntry entry) {
    this(entry.getTitle(), entry.getURL(), false);
  }

  public boolean isPassiveFtp() {
    return passiveFtp;
  }

  public void setPassiveFtp(boolean passiveFtp) {
    this.passiveFtp = passiveFtp;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || (obj.getClass() != this.getClass())) {
      return false;
    }

    // object must be FTPURLEntry at this point
    FTPURLEntry fue = (FTPURLEntry) obj;

    return (passiveFtp == fue.passiveFtp) &&
        ((getURL() == fue.getURL()) || ((fue != null) && getURL().equals(fue.getURL()))) &&
        ((getTitle() == fue.getTitle()) || ((fue != null) && getTitle().equals(fue.getTitle())));
  }

  @Override
  public Object clone() {
    FTPURLEntry fue = (FTPURLEntry) super.clone();
    fue.passiveFtp = passiveFtp;
    return fue;
  }

  @Override
  public String toString() {
    return String.format("%s, passiveMode=%s", super.toString(), passiveFtp);
  }
}
