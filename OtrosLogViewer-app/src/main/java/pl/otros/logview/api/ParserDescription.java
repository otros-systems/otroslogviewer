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
package pl.otros.logview.api;

import javax.swing.*;

public class ParserDescription {

  private String displayName;
  private String description;
  private int menmonic;
  private String keyStrokeAccelelator;
  private Icon icon;
  private String charset;
  private String file;

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getMenmonic() {
    return menmonic;
  }

  public void setMenmonic(int menmonic) {
    this.menmonic = menmonic;
  }

  public String getKeyStrokeAccelelator() {
    return keyStrokeAccelelator;
  }

  public void setKeyStrokeAccelelator(String keyStrokeAccelelator) {
    this.keyStrokeAccelelator = keyStrokeAccelelator;
  }

  public Icon getIcon() {
    return icon;
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

}
