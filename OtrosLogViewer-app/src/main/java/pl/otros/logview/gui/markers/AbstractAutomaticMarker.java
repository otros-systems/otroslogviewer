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
package pl.otros.logview.gui.markers;

import pl.otros.logview.api.pluginable.AutomaticMarker;
import pl.otros.logview.api.model.MarkerColors;

public abstract class AbstractAutomaticMarker implements AutomaticMarker {

  protected String name;
  protected String description;
  protected String[] groups;
  protected MarkerColors markerColors;

  public AbstractAutomaticMarker(String name, String description, MarkerColors markerColors, String... groups) {
    this.name = name;
    this.description = description;
    this.groups = groups;
    this.markerColors = markerColors;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String[] getMarkerGroups() {
    return groups;
  }

  @Override
  public MarkerColors getColors() {
    return markerColors;
  }

  @Override
  public String getPluginableId() {
    return this.getClass().getName();
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int getApiVersion() {
    return AUTOMATIC_MARKER_VERSION_1;
  }

}
