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
package pl.otros.logview.gui.actions;

import pl.otros.logview.gui.markers.editor.StringRegexMarkerEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShowStringRegexMarkerEditorActionListener implements ActionListener {

  private JFrame parent;

  public ShowStringRegexMarkerEditorActionListener(JFrame parent) {
    super();
    this.parent = parent;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {

    JDialog dialog = new JDialog(parent, true);
    dialog.setTitle("String and Regex marker editor");
    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(new StringRegexMarkerEditor());
    dialog.pack();
    int x = 0;
    int y = 0;
    Dimension parentDimension = parent.getSize();
    Point parentLocation = parent.getLocation();

    x = parentLocation.x + parentDimension.width / 2 - dialog.getWidth() / 2;
    y = parentLocation.y + parentDimension.height / 2 - dialog.getWidth() / 2;
    dialog.setLocation(x, y);
    dialog.setResizable(false);
    dialog.setVisible(true);

  }

}
