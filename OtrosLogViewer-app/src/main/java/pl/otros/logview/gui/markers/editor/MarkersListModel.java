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
package pl.otros.logview.gui.markers.editor;

import pl.otros.logview.gui.markers.AutomaticMarker;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.logview.pluginable.PluginableElementEventListener;
import pl.otros.logview.pluginable.PluginableElementsContainer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class MarkersListModel extends AbstractListModel implements PluginableElementEventListener<AutomaticMarker> {

  private ArrayList<AutomaticMarker> list;
  private Comparator<AutomaticMarker> makrerComparator;
  private PluginableElementsContainer<AutomaticMarker> markersContainser;

  public MarkersListModel(Collection<AutomaticMarker> markersList) {
    markersContainser = AllPluginables.getInstance().getMarkersContainser();
    list = new ArrayList<AutomaticMarker>(markersList.size());
    makrerComparator = new Comparator<AutomaticMarker>() {

      @Override
      public int compare(AutomaticMarker o1, AutomaticMarker o2) {
        return o1.getName().compareTo(o2.getName());
      }
    };
    list.addAll(markersList);
    Collections.sort(list, makrerComparator);
  }

  @Override
  public int getSize() {
    return list.size();
  }

  @Override
  public Object getElementAt(int index) {
    return list.get(index);
  }

  @Override
  public void elementAdded(AutomaticMarker element) {
    list.clear();
    list.addAll(markersContainser.getElements());
    Collections.sort(list, makrerComparator);
    fireContentsChanged(this, 0, list.size() - 1);

  }

  @Override
  public void elementRemoved(AutomaticMarker element) {
    list.clear();
    list.addAll(markersContainser.getElements());
    Collections.sort(list, makrerComparator);
    fireContentsChanged(this, 0, list.size() - 1);

  }

  @Override
  public void elementChanged(AutomaticMarker element) {
    list.clear();
    list.addAll(markersContainser.getElements());
    Collections.sort(list, makrerComparator);
    fireContentsChanged(this, 0, list.size() - 1);

  }

}
