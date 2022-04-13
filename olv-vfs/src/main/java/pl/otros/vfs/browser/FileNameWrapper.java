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

package pl.otros.vfs.browser;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.AbstractFileName;

public class FileNameWrapper extends AbstractFileName {

  protected FileName fileName;

  public FileNameWrapper(FileName fileName) {
    super(fileName.getScheme(), fileName.getPath(), fileName.getType());
    this.fileName = fileName;
  }

  @Override
  public String getBaseName() {
    return fileName.getBaseName();
  }

  @Override
  public int getDepth() {
    return fileName.getDepth();
  }

  @Override
  public String getExtension() {
    return fileName.getExtension();
  }

  @Override
  public String getFriendlyURI() {
    return fileName.getFriendlyURI();
  }

  @Override
  public FileName getParent() {
    return fileName.getParent();
  }

  @Override
  public String getPath() {
    return fileName.getPath();
  }

  @Override
  public String getPathDecoded() throws FileSystemException {
    return fileName.getPathDecoded();
  }

  @Override
  public String getRelativeName(FileName name) throws FileSystemException {
    return fileName.getRelativeName(name);
  }

  @Override
  public FileName getRoot() {
    return fileName.getRoot();
  }

  @Override
  public String getRootURI() {
    return fileName.getRootURI();
  }

  @Override
  public String getScheme() {
    return fileName.getScheme();
  }

  @Override
  public FileType getType() {
    return fileName.getType();
  }

  @Override
  public String getURI() {
    return fileName.getURI();
  }

  @Override
  public boolean isAncestor(FileName ancestor) {
    return fileName.isAncestor(ancestor);
  }

  @Override
  public boolean isDescendent(FileName descendent) {
    return fileName.isDescendent(descendent);
  }

  @Override
  public boolean isDescendent(FileName descendent, NameScope nameScope) {
    return fileName.isDescendent(descendent, nameScope);
  }

  @Override
  public int compareTo(FileName o) {
    return fileName.compareTo(o);
  }

  @Override
  public FileName createName(String absPath, FileType type) {
    return ((AbstractFileName) fileName).createName(absPath, type);
  }

  @Override
  protected void appendRootUri(StringBuilder buffer, boolean addPassword) {

  }
}
