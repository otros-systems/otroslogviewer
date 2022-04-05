/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
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
package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;

import java.util.Collection;
import java.util.Optional;

public class TailLogWithAutoDetectActionListener extends TailLogActionListener {

  public TailLogWithAutoDetectActionListener(OtrosApplication otrosApplication) {
    super(otrosApplication, new DetectOnTheFlyLogImporter(otrosApplication.getAllPluginables().getLogImportersContainer().getElements()));
  }

  @Override
  protected TableColumns[] determineTableColumnsToUse(LoadingInfo loadingInfo, LogImporter importer) {
    byte[] inputStreamBufferedStart = loadingInfo.getInputStreamBufferedStart();

    if(importer instanceof DetectOnTheFlyLogImporter) {
      Collection<LogImporter> logImporters = ((DetectOnTheFlyLogImporter) importer).getLogImporters();
      Optional<LogImporter> detectLogImporter = Utils.detectLogImporter(logImporters, inputStreamBufferedStart);
      if (detectLogImporter.isPresent()) {
        return super.determineTableColumnsToUse(loadingInfo, detectLogImporter.get());
      }
    }

    return super.determineTableColumnsToUse(loadingInfo, importer);
  }

}
