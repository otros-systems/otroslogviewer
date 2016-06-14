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

package pl.otros.logview.api.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 */
public class PossibleLogImporters {
  private Optional<LogImporter> logImporter = Optional.empty();
  private List<LogImporter> availableImporters = new ArrayList<>();

  public List<LogImporter> getAvailableImporters() {
    return availableImporters;
  }

  public void setAvailableImporters(List<LogImporter> availableImporters) {
    this.availableImporters = availableImporters;
  }

  public Optional<LogImporter> getLogImporter() {
    return logImporter;
  }

  public void setLogImporter(Optional<LogImporter> logImporter) {
    this.logImporter = logImporter;
  }
}
