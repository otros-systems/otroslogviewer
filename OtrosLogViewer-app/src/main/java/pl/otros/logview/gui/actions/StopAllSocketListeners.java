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
package pl.otros.logview.gui.actions;

import net.miginfocom.swing.MigLayout;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.reader.SocketLogReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopAllSocketListeners extends OtrosAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(StopAllSocketListeners.class.getName());
	private Collection<SocketLogReader> logReaders = null;

	public StopAllSocketListeners(OtrosApplication otrosApplication, Collection<SocketLogReader> logReaders) {
		super(otrosApplication);
		this.logReaders = logReaders;

		putValue(Action.NAME, "Stop all socket listeners");
		putValue(Action.SHORT_DESCRIPTION, "Stop all socket listeners.");
		putValue(Action.SMALL_ICON, Icons.PLUGIN_DISCONNECT);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (logReaders.size() == 0) {
			JOptionPane.showMessageDialog((Component) arg0.getSource(), "OtrosLogViewer is not listening on socket");
			return;
		}

		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Are you sure, that you want to close following sockets?"), "wrap");
		panel.add(new JLabel("Opened sockets"), "wrap, growx");
		JTable jTable = new JTable(logReaders.size(), 2);
		jTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Log importer");
		jTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Port");
		int row = 0;
		for (SocketLogReader socketLogReader : logReaders) {
			jTable.setValueAt(socketLogReader.getLogImporter().getName(), row, 0);
			jTable.setValueAt(Integer.toString(socketLogReader.getPort()), row, 1);
			row++;
		}
		JScrollPane jScrollPane = new JScrollPane(jTable);
		panel.add(jScrollPane, "wrap, span");

		int showConfirmDialog = JOptionPane.showConfirmDialog(null, panel, "Choose log importer and port", JOptionPane.YES_NO_OPTION);
		if (showConfirmDialog != JOptionPane.YES_OPTION) {
			return;
		}

		int failedCount = 0;
		for (SocketLogReader socketLogReader : logReaders) {
			try {
				socketLogReader.close();
			} catch (IOException e) {
				failedCount++;
				LOGGER.warn("Error closing socket " + e.getMessage());
			}
		}
		if (failedCount == 0) {
			getOtrosApplication().getStatusObserver().updateStatus("All socket closed");
			LOGGER.info(String.format("Closed %d sockets", logReaders.size()));
		} else {
            getOtrosApplication().getStatusObserver().updateStatus(String.format("Failed to close %d socket(s) ", failedCount), StatusObserver.LEVEL_WARNING);
		}
		logReaders.clear();

	}

}
