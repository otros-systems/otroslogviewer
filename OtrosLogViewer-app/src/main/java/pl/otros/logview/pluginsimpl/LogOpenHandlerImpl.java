/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.pluginsimpl;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import pl.otros.logview.api.plugins.LogOpenHandler;
import pl.otros.logview.api.plugins.PluginContext;
import pl.otros.vfs.browser.util.VFSUtils;

import javax.swing.*;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class LogOpenHandlerImpl implements LogOpenHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogOpenHandlerImpl.class.getName());

  private final PluginContext pluginContext;

  public LogOpenHandlerImpl(PluginContext pluginContext) {
    this.pluginContext = pluginContext;
  }

  @Override
  public void openLogs(final String tabName, final FileObject... fileObjects) {
    SwingWorker<Void, String> worker = new OpenLogsSwingWorker(pluginContext, tabName,fileObjects);
    new Thread(worker).start();
  }

  @Override
  public void openLogs(String tabName, String... uris) {
    ArrayList<FileObject> list = new ArrayList<>();
    for (String uri : uris) {
      try {
        list.add(VFSUtils.resolveFileObject(uri));
      } catch (FileSystemException e) {
        LOGGER.error("Can't resolve uri " + uri,e);
      }
    }
    FileObject[] fileObjects = new FileObject[0];
    fileObjects = list.toArray(fileObjects);
    openLogs(tabName, fileObjects);
  }

}
