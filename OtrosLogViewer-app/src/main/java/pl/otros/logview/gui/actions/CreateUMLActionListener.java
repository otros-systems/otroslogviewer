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
package pl.otros.logview.gui.actions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateUMLActionListener implements ActionListener {


  @Override
  public void actionPerformed(ActionEvent arg0) {
    try {
      /*
       * LogViewInternalFrame lvFrame = FrameManger.getInstance().getActiveLogViewInternalFrame(); if (lvFrame == null) { return; }
       * 
       * LogDataTableModel dataTableModel = lvFrame.getDataTableModel(); JTable table = lvFrame.getLogViewPanel().getTable(); LogdataConverter converter = new
       * LogdataConverter(); UMLModel umlModel = converter.createJComponent(dataTableModel.getLogData());
       * 
       * JFrame frame = new JFrame("UML"); frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       * 
       * java.awt.Container cp = frame.getContentPane();
       * 
       * cp.add(umlModel.getJComponent());
       * 
       * frame.setSize(400, 400); frame.setLocation(400, 50); frame.setVisible(true);
       * 
       * LogViewTableWithUMLSynchronizer synchronizer = new LogViewTableWithUMLSynchronizer(table, umlModel);
       */
      throw new Exception("Not implemented!");
    } catch (Exception e) {
      JOptionPane.showMessageDialog((Component) arg0.getSource(), "Error Creating UML " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }

  }

}
