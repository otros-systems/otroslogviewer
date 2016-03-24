/*******************************************************************************
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
 ******************************************************************************/
package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.Utils;

import java.util.Collection;

public class TailLogWithAutoDetectActionListener extends TailLogActionListener {

//  private final

  public TailLogWithAutoDetectActionListener(OtrosApplication otrosApplication) {
    super(otrosApplication, new DetectOnTheFlyLogImporter(otrosApplication.getAllPluginables().getLogImportersContainer().getElements()));
  }

  @Override
  protected TableColumns[] determineTableColumnsToUse(LoadingInfo loadingInfo, LogImporter importer) {
		Collection<LogImporter> logImporters = getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements();
    byte[] inputStreamBufferedStart = loadingInfo.getInputStreamBufferedStart();
    LogImporter detectLogImporter = Utils.detectLogImporter(logImporters, inputStreamBufferedStart);
    TableColumns[] determineTableColumnsToUse = super.determineTableColumnsToUse(loadingInfo, importer);
    if (detectLogImporter != null) {
      determineTableColumnsToUse = super.determineTableColumnsToUse(loadingInfo, detectLogImporter);
    }
    return determineTableColumnsToUse;
  }

}
