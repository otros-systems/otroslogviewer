/*
 * Copyright 2012 Krzysztof Otrebski
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

package pl.otros.logview.api.pluginable;

import javax.swing.text.Style;

/**
 * Class contains information about style.
 */
public class MessageFragmentStyle {
  private int offset;
  private int length;
  private Style style;
  private boolean replace;
  private boolean searchResult;


  public MessageFragmentStyle() {
  }

  public MessageFragmentStyle(int offset, int length, Style style, boolean replace) {
    this.style = style;
    this.length = length;
    this.offset = offset;
    this.replace = replace;
  }

  public MessageFragmentStyle(int offset, int length, Style style, boolean replace, boolean searchResult) {
    this(offset, length, style, replace);
    this.searchResult = searchResult;
  }

  public boolean isSearchResult() {
    return searchResult;
  }

  public void setSearchResult(boolean searchResult) {
    this.searchResult = searchResult;
  }

  public Style getStyle() {
    return style;
  }

  public void setStyle(Style style) {
    this.style = style;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public boolean isReplace() {
    return replace;
  }

  public void setReplace(boolean replace) {
    this.replace = replace;
  }

  public MessageFragmentStyle shift(int shift){
    return new MessageFragmentStyle(offset+shift,this.length,style,replace,searchResult);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MessageFragmentStyle)) return false;

    MessageFragmentStyle that = (MessageFragmentStyle) o;

    if (length != that.length) return false;
    if (offset != that.offset) return false;
    if (replace != that.replace) return false;
    return !(style != null ? !style.equals(that.style) : that.style != null);

  }

  @Override
  public int hashCode() {
    int result = offset;
    result = 31 * result + length;
    result = 31 * result + (style != null ? style.hashCode() : 0);
    result = 31 * result + (replace ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "MessageFragmentStyle{" +
      "offset=" + offset +
      ", length=" + length +
      ", style=" + style +
      ", replace=" + replace +
      ", searchResult=" + searchResult +
      '}';
  }
}
