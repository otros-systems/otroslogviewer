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
 * @author Dirk Moebius (JEdit)
 * @author Yves Zoundi <yveszoundi at users dot sf dot net>
 * @version 0.0.1
 */
public class TitledURLEntry implements Cloneable {
  private String title = null;
  private String url = null;
  private int scrollBarPos = -1;

  /**
   * new TitledURLEntry with title and url
   *
   * @param title
   * @param url
   */
  public TitledURLEntry(String title, String url) {
    this.title = title;
    this.url = url;
  }


  /**
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return
   */
  public String getURL() {
    return url;
  }

  /**
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @param url
   */
  public void setURL(String url) {
    this.url = url;
  }

  /**
   * @return
   */
  public int getScrollBarPos() {
    return scrollBarPos;
  }

  /**
   * @param newPos
   */
  public void setScrollBarPos(int newPos) {
    scrollBarPos = newPos;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash;
    hash = (31 * hash) + ((null == title) ? 0 : title.hashCode());
    hash = 31 + hash + ((null == url) ? 0 : url.hashCode());

    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || (obj.getClass() != this.getClass())) {
      return false;
    }

    // object must be TitledURLEntry at this point
    TitledURLEntry tue = (TitledURLEntry) obj;

    return ((url == tue.url) || ((tue != null) && url.equals(tue.url))) &&
        ((title == tue.title) || ((tue != null) && title.equals(tue.title)));
  }

  @Override
  public Object clone() {
    TitledURLEntry tue = null;

    try {
      tue = (TitledURLEntry) super.clone();
      tue.title = title;
      tue.url = url;

      return tue;
    } catch (Exception e) {
      tue = new TitledURLEntry(title, url);
      tue.scrollBarPos = this.scrollBarPos;
    }

    return tue;
  }

  @Override
  public String toString() {
    return url;
  }
}
