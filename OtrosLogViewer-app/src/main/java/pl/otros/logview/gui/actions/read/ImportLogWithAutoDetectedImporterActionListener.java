/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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
package pl.otros.logview.gui.actions.read;

import pl.otros.logview.api.OtrosApplication;

public class ImportLogWithAutoDetectedImporterActionListener extends ImportLogActionListener {


  public ImportLogWithAutoDetectedImporterActionListener(OtrosApplication otrosApplication) {
    super(otrosApplication, "Open log with autodetection");
  }

  @Override
  protected LogImporterProvider getLogImporterProvider() {
    return new AutoDetectingImporterProvider(getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements());
  }
}
