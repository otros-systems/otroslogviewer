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
package pl.otros.logview.gui.actions.read;

import pl.otros.logview.importer.LogImporter;
import pl.otros.logview.io.LoadingInfo;
import pl.otros.logview.io.Utils;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoDetectingImporterProvider implements LogImporterProvider {

  private static final Logger LOGGER = Logger.getLogger(AutoDetectingImporterProvider.class.getName());
  private Collection<LogImporter> importers;

  public AutoDetectingImporterProvider(Collection<LogImporter> importers) {
    this.importers = importers;
  }

  @Override
  public LogImporter getLogImporter(LoadingInfo openFileObject) {
    try {
      LOGGER.info("Autodetecting LogImporter");
      return Utils.detectLogImporter(importers, openFileObject.getInputStreamBufferedStart());
    } catch (Exception e) {
      LOGGER.warning("LogImproter can't be detected: " + e.getMessage());
      ImportLogWithAutoDetectedImporterActionListener.LOGGER.log(Level.WARNING, "Could not read probe from log file " + openFileObject.getFriendlyUrl()
          + " and thus could not determine importer.", e);
      return null;
    }
  }
}
