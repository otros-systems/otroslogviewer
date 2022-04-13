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

import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.JOptionPane;

public class TailLogWithComboActionListener extends TailLogActionListener {

//  private final

  public TailLogWithComboActionListener(OtrosApplication otrosApplication) {
    super(otrosApplication, new DetectOnTheFlyLogImporter(otrosApplication.getAllPluginables().getLogImportersContainer().getElements()));
  }

  @Override
  protected TableColumns[] determineTableColumnsToUse(LoadingInfo loadingInfo, LogImporter importer) {
    Collection<LogImporter> logImporters = getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements();
    byte[] inputStreamBufferedStart = loadingInfo.getInputStreamBufferedStart();
    Optional<LogImporter> detectLogImporter = Utils.detectLogImporter(logImporters, inputStreamBufferedStart);
    List<LogImporter> possibleImporters = Utils.detectPossibleLogImporter(logImporters, inputStreamBufferedStart).getAvailableImporters();
    
    if (possibleImporters != null && possibleImporters.size() > 1) {
        LogImporter logImporter = (LogImporter)JOptionPane.showInputDialog(
                null,
                "Select log parser",
                "Select log parser",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibleImporters.toArray(),
                detectLogImporter);

        detectLogImporter = Optional.of(logImporter);
        
        setImporter(logImporter);    	
    }

    
    TableColumns[] determineTableColumnsToUse = super.determineTableColumnsToUse(loadingInfo, importer);
    if (detectLogImporter.isPresent()) {
      determineTableColumnsToUse = super.determineTableColumnsToUse(loadingInfo, detectLogImporter.get());
    }
    return determineTableColumnsToUse;
  }

}
