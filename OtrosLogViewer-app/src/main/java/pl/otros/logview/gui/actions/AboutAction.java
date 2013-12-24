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

import pl.otros.logview.VersionUtil;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.JAnimatedLogo;
import pl.otros.logview.gui.OtrosApplication;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutAction extends OtrosAction {

	private String build = "?";
	private static final Logger LOGGER = Logger.getLogger(AboutAction.class.getName());
	private JTextPane textArea;

	private Style defaultStyle = null;
	private Style mainStyle = null;
	private Style licenceStyle = null;
	private Style titleStyle = null;
	private StyleContext sc;
	private JAnimatedLogo animatedLogo;
	private JPanel panel;

	public AboutAction(OtrosApplication otrosApplication) {
		super(otrosApplication);
		this.putValue(SMALL_ICON, Icons.LOGO_OTROS_16);
		try {
			build = VersionUtil.getRunningVersion();
		} catch (IOException e) {
			LOGGER.severe("Problem with checking running version: " + e.getMessage());
		}

		animatedLogo = new JAnimatedLogo();
		textArea = new JTextPane();
		textArea.setText("");
		textArea.setEditable(false);
		textArea.setBackground(new JLabel().getBackground());

		sc = new StyleContext();
		defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		mainStyle = sc.addStyle("MainStyle", defaultStyle);
		StyleConstants.setFontSize(mainStyle, 12);
		StyleConstants.setForeground(mainStyle, Color.BLACK);

		licenceStyle = sc.addStyle("classMethod", null);
		StyleConstants.setFontFamily(licenceStyle, "monospaced");
		StyleConstants.setForeground(licenceStyle, Color.BLUE);
		titleStyle = sc.addStyle("note", mainStyle);
		StyleConstants.setFontFamily(titleStyle, "arial");
		StyleConstants.setBold(titleStyle, true);
		StyleConstants.setFontSize(titleStyle, 20);

		try {
			fillText();
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE,"Bad location when filling text.",e);
		}
		panel = new JPanel(new FlowLayout());
		panel.add(animatedLogo);
		panel.add(textArea);
	}

	private void fillText() throws BadLocationException {
		textArea.setText("");
		StyledDocument sd = textArea.getStyledDocument();
		sd.insertString(0, "OtrosLogViewer\n", titleStyle);
		sd.insertString(sd.getLength(), "Build: " + build + "\n", mainStyle);
		sd.insertString(sd.getLength(), "Project web page: http://code.google.com/p/otroslogviewer/\n", mainStyle);
		sd.insertString(sd.getLength(), "Program documentation: http://code.google.com/p/otroslogviewer/wiki/Introduction?tm=6 \n", mainStyle);

		sd.insertString(sd.getLength(), "License: Apache Commons 2.0", licenceStyle);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		animatedLogo.start();
		OtrosApplication otrosApplication = getOtrosApplication();
		JFrame applicationJFrame = otrosApplication.getApplicationJFrame();
		JOptionPane.showMessageDialog(applicationJFrame, panel, "About", JOptionPane.INFORMATION_MESSAGE, new EmptyIcon());
		animatedLogo.stop();

	}

}
