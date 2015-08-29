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
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.BufferingLogDataCollectorProxy;
import pl.otros.logview.gui.*;
import pl.otros.logview.gui.actions.TailLogActionListener.ParsingContextStopperForClosingTab;
import pl.otros.logview.gui.table.TableColumns;
import pl.otros.logview.importer.Log4jSerilizedLogImporter;
import pl.otros.logview.importer.LogImporter;
import pl.otros.logview.parser.ParsingContext;

import javax.net.SocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

public class ConnectToSocketHubAppenderAction extends OtrosAction {

	private static final int RECONNECT_TIME = 20 * 1000;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectToSocketHubAppenderAction.class.getName());

	private BufferingLogDataCollectorProxy logDataCollector;

  private String host = "127.0.0.1";
	private int port = 50000;
	private Socket socket;

	public ConnectToSocketHubAppenderAction(OtrosApplication otrosApplication) {
		super(otrosApplication);
		putValue(Action.NAME, "Connect to Log4j socket hub");
		putValue(Action.SHORT_DESCRIPTION, "Connect to Log4j SocketHubAppender");
		putValue(Action.LONG_DESCRIPTION, "Connect to Log4j SocketHubAppender");
		putValue(SMALL_ICON, Icons.PLUGIN_CONNECT);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		boolean accepted = chooseLogImporter();
		if (!accepted) {
			return;
		}

    LogViewPanelWrapper logViewPanelWrapper = new LogViewPanelWrapper("Socket", null, TableColumns.values(), getOtrosApplication());

		logViewPanelWrapper.goToLiveMode();
		BaseConfiguration configuration = new BaseConfiguration();
		configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
		configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);
		logDataCollector = new BufferingLogDataCollectorProxy(logViewPanelWrapper.getDataTableModel(), 4000, configuration);
//		JTabbedPane jTabbedPane = getOtrosApplication().getJTabbedPane();
//		int tabCount = jTabbedPane.getTabCount();
		String hostPort = "Log4j SocketHub " + host + ":" + port;

		try {

			final LogImporter logImporter = new Log4jSerilizedLogImporter();
			logImporter.init(new Properties());
			final ParsingContext parsingContext = new ParsingContext(hostPort, hostPort);

			logImporter.initParsingContext(parsingContext);
			TailLogActionListener.ParsingContextStopperForClosingTab contextStopperForClosingTab = new ParsingContextStopperForClosingTab(parsingContext);
			TailLogActionListener.ReadingStopperForRemove readingStopperForRemove = new TailLogActionListener.ReadingStopperForRemove(contextStopperForClosingTab,
					logDataCollector);
			logViewPanelWrapper.addHierarchyListener(readingStopperForRemove);

            getOtrosApplication().addClosableTab(hostPort,hostPort,Icons.PLUGIN_CONNECT, logViewPanelWrapper,true);

			Runnable r = () -> {
        InetAddress inetAddress = socket.getInetAddress();
        int port2 = socket.getPort();
        InputStream inputStream = null;
        Socket s = socket;
        while (parsingContext.isParsingInProgress()) {
          try {
            inputStream = s.getInputStream();
            BufferedInputStream bin = new BufferedInputStream(inputStream);
            LOGGER.info(String.format("Connect to SocketHubAppender to %s:%d", inetAddress.getHostAddress(), port2));
            logImporter.importLogs(bin, logDataCollector, parsingContext);
            getOtrosApplication().getStatusObserver().updateStatus("Loading logs from Log4j SocketHubAppender finished", StatusObserver.LEVEL_WARNING);
          } catch (IOException e1) {
            LOGGER.warn(String.format("Problem with connecting to %s:%d: %s", inetAddress.getHostAddress(), port2, e1.getMessage()));
          }
          try {
            LOGGER.debug("Reconnecting in " + RECONNECT_TIME + "ms");
            Thread.sleep(RECONNECT_TIME);
          } catch (InterruptedException e) {
            LOGGER.warn("Waiting thread interrupted" + e.getMessage());
          }
          if (parsingContext.isParsingInProgress()) {
            try {
              LOGGER.debug(String.format("Connecting to Log4j SocketHubAppender at %s:%d", inetAddress.getHostName(), port2));
              s = new Socket(inetAddress, port2);
            } catch (IOException e) {
              LOGGER.warn(String.format("Problem with connecting to %s:%d: %s", inetAddress.getHostAddress(), port2, e.getMessage()));
            }
          }
        }
        LOGGER.info(String.format("Importing from %s:%d is finished", inetAddress.getHostName(), port2));
      };
			new Thread(r, hostPort).start();

		} catch (Exception e) {
			JOptionPane.showMessageDialog((Component) arg0.getSource(), "Error importing logs from " + hostPort, "Error importing logs", JOptionPane.ERROR_MESSAGE);

		}

	}

	private boolean chooseLogImporter() {
		DataConfiguration configuration = getOtrosApplication().getConfiguration();
		List<Object> list1 = configuration.getList(ConfKeys.SOCKET_HUB_APPENDER_ADDRESSES);
		configuration.getInt(ConfKeys.SOCKET_HUB_APPENDER_ADDRESSES_MAX_COUNT,20);

		Vector<String> recent = list1.stream().map(Object::toString).collect(Collectors.toCollection(Vector::new));

		JXComboBox box = new JXComboBox(recent);
		box.setEditable(true);
		AutoCompleteDecorator.decorate(box);

		MigLayout migLayout = new MigLayout();
		JPanel panel = new JPanel(migLayout);
		panel.add(new JLabel("Host name:port"));
		panel.add(box, "wrap, width 200:220:440");

		while (true) {
			String[] options = {"Connect", "Cancel"};
			int showConfirmDialog = JOptionPane.showOptionDialog(getOtrosApplication().getApplicationJFrame(), panel, "Enter host name and port",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (showConfirmDialog != JOptionPane.OK_OPTION) {

				return false;
			}

			try {
				String hostAndPortString = box.getSelectedItem().toString().trim();
				socket = tryToConnectToSocket(configuration, hostAndPortString, SocketFactory.getDefault());
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(panel, host + " is unknown host name", "Error", JOptionPane.ERROR_MESSAGE);
				continue;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(panel, "Cannot connect to host " + host + ":" + port, "Error", JOptionPane.ERROR_MESSAGE);
				continue;
			}  catch (NumberFormatException e){
				JOptionPane.showMessageDialog(panel, "Can't parse port number.", "Error", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			return true;
		}

	}

	protected Socket tryToConnectToSocket(DataConfiguration configuration, String hostAndPortString, SocketFactory socketFactory) throws IOException {
		List<Object> list1 = configuration.getList(ConfKeys.SOCKET_HUB_APPENDER_ADDRESSES);
		String[] hostPort = hostAndPortString.split(":");
		host = hostPort[0];
		if (hostPort.length>1){
			port = Integer.parseInt(hostPort[1]);
		}    else {
			port = 4560;
		}

		Socket socket = socketFactory.createSocket(host, port);
		if (list1.contains(hostAndPortString)) {
			list1.remove(hostAndPortString);
		}
		list1.add(0, hostAndPortString);
		if (list1.size()>30){
			list1.remove(list1.size()-1);
		}
		configuration.setProperty(ConfKeys.SOCKET_HUB_APPENDER_ADDRESSES,list1);
		return socket;
	}

}
