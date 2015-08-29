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

import org.apache.commons.io.IOUtils;
import pl.otros.logview.gui.markers.AutomaticMarker;
import pl.otros.logview.gui.markers.PropertyFileAbstractMarker;
import pl.otros.logview.gui.markers.RegexMarker;
import pl.otros.logview.gui.markers.StringMarker;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.logview.pluginable.PluginableElementsContainer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class SaveMarkerAction extends AbstractAction {

  private final MarkerEditor editor;
  private final PluginableElementsContainer<AutomaticMarker> markersContainser;

  public SaveMarkerAction(MarkerEditor editor) {
    super();
    putValue(NAME, "Save");
    this.editor = editor;
    markersContainser = AllPluginables.getInstance().getMarkersContainser();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      Properties markerPropertiesFromView = editor.getMarkerPropertiesFromView();
      String file = markerPropertiesFromView.getProperty(PropertyFileAbstractMarker.FILE);
      PropertyFileAbstractMarker m = null;
      String type = markerPropertiesFromView.getProperty(PropertyFileAbstractMarker.TYPE);
      if (PropertyFileAbstractMarker.TYPE_STRING.equals(type)) {
        m = new StringMarker(markerPropertiesFromView);
      } else {
        m = new RegexMarker(markerPropertiesFromView);
      }
      m.setFileName(file);
      markersContainser.addElement(m);
      FileOutputStream bout = new FileOutputStream(new File(AllPluginables.USER_MARKERS, file));
      markerPropertiesFromView.store(bout, "File genereated using GUI editor");
      IOUtils.closeQuietly(bout);

    } catch (Exception e1) {
      e1.printStackTrace();
      JOptionPane.showMessageDialog(editor, "Cannot save marker: " + e1.getMessage(), "Error saving marker!", JOptionPane.ERROR_MESSAGE);
    }
  }
}
