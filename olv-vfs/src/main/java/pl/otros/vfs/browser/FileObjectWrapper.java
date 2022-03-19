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

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.operations.FileOperations;

import java.net.URL;
import java.util.List;

public class FileObjectWrapper implements FileObject {
  protected FileObject parent;

  public FileObjectWrapper(FileObject parent) {
    super();
    this.parent = parent;
  }

  public FileName getName() {
    return parent.getName();
  }

  public URL getURL() throws FileSystemException {
    return parent.getURL();
  }

  public boolean exists() throws FileSystemException {
    return parent.exists();
  }

  public boolean isHidden() throws FileSystemException {
    return parent.isHidden();
  }

  public boolean isReadable() throws FileSystemException {
    return parent.isReadable();
  }

  public boolean isWriteable() throws FileSystemException {
    return parent.isWriteable();
  }

  public FileType getType() throws FileSystemException {
    return parent.getType();
  }

  public FileObject getParent() throws FileSystemException {
    return parent.getParent();
  }

  public FileSystem getFileSystem() {
    return parent.getFileSystem();
  }

  public FileObject[] getChildren() throws FileSystemException {
    return parent.getChildren();
  }

  public FileObject getChild(String name) throws FileSystemException {
    return parent.getChild(name);
  }

  public FileObject resolveFile(String name, NameScope scope)
      throws FileSystemException {
    return parent.resolveFile(name, scope);
  }

  public FileObject resolveFile(String path) throws FileSystemException {
    return parent.resolveFile(path);
  }

  public FileObject[] findFiles(FileSelector selector)
      throws FileSystemException {
    return parent.findFiles(selector);
  }

  public void findFiles(FileSelector selector, boolean depthwise,
                        List<FileObject> selected) throws FileSystemException {
    parent.findFiles(selector, depthwise, selected);
  }

  public boolean delete() throws FileSystemException {
    return parent.delete();
  }

  public int delete(FileSelector selector) throws FileSystemException {
    return parent.delete(selector);
  }

  public void createFolder() throws FileSystemException {
    parent.createFolder();
  }

  public void createFile() throws FileSystemException {
    parent.createFile();
  }

  public void copyFrom(FileObject srcFile, FileSelector selector)
      throws FileSystemException {
    parent.copyFrom(srcFile, selector);
  }

  public void moveTo(FileObject destFile) throws FileSystemException {
    parent.moveTo(destFile);
  }

  public boolean canRenameTo(FileObject newfile) {
    return parent.canRenameTo(newfile);
  }

  public FileContent getContent() throws FileSystemException {
    return parent.getContent();
  }

  public void close() throws FileSystemException {
    parent.close();
  }

  public void refresh() throws FileSystemException {
    parent.refresh();
  }

  public boolean isAttached() {
    return parent.isAttached();
  }

  public boolean isContentOpen() {
    return parent.isContentOpen();
  }

  public FileOperations getFileOperations() throws FileSystemException {
    return parent.getFileOperations();
  }
}
