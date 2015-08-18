package pl.otros.logview.gui.actions;

import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

public class GoToDonatePageAction extends OtrosAction {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(GoToDonatePageAction.class.getName());

  private static final String DONATE_URL = "https://github.com/otros-systems/otroslogviewer/wiki/DonateAndDonors";

  public GoToDonatePageAction(OtrosApplication otrosApplication) {
    super(otrosApplication);
    this.putValue(NAME, "Support us by donation using PayPal or Bitcoin");
    this.putValue(SHORT_DESCRIPTION, "Go to the donation page");
    this.putValue(SMALL_ICON, Icons.CREDIT_CARDS);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      LOGGER.debug("Opening donation page");
      Desktop.getDesktop().browse(new URI(DONATE_URL));
    } catch (IOException e1) {
      LOGGER.info(String.format("Can't open donate url due to IOException: %s", e1.getMessage()));
      JOptionPane.showMessageDialog(null, "Can't open donation page: " + e1.getMessage());

    } catch (URISyntaxException e1) {
      LOGGER.info(String.format("Can't open donate url,  URI syntax error: %s", e1.getMessage()));
      JOptionPane.showMessageDialog(null, "Can't open donation page: " + e1.getMessage());
    }
  }
}
