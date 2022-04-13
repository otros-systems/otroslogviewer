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
package pl.otros.logview.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.loading.LogLoader;
import pl.otros.logview.api.loading.LogLoadingSession;
import pl.otros.logview.api.loading.SocketSource;
import pl.otros.logview.api.model.LogDataCollector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class SocketLogReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(SocketLogReader.class.getName());
  private ServerSocket serverSocket;
  private final StatusObserver observer;
  private final LogLoader logLoader;
  private final LogDataCollector logDataCollector;
  private final LogImporter logImporter;
  private final int port;
  private Set<LogLoadingSession> loadingSessionSet;

  public SocketLogReader(LogImporter logImporter, LogDataCollector logDataCollector, StatusObserver observer, LogLoader logLoader,int port) {
    super();
    this.logImporter = logImporter;
    this.logDataCollector = logDataCollector;
    this.observer = observer;
    this.logLoader = logLoader;
    this.port = port;
    loadingSessionSet = new HashSet<>();
  }

  public void close() throws IOException {
    if (serverSocket != null) {
      serverSocket.close();
      serverSocket = null;
    }
    loadingSessionSet.forEach(logLoader::close);
  }

  public void start() throws Exception {
    serverSocket = new ServerSocket(port,50,InetAddress.getByAddress(new byte[]{0,0,0,0}));
    Runnable r = () -> {
      try {
        while (true) {
          Socket s = serverSocket.accept();
          final SocketSource socketSource = new SocketSource(s);
          final LogLoadingSession logLoadingSession = logLoader.startLoading(socketSource, logImporter, logDataCollector);
          loadingSessionSet.add(logLoadingSession);
        }
      } catch (IOException e) {
        if (isClosed()) {
          LOGGER.info("Listening on socket closed.");
        } else {
          LOGGER.warn("Problem with listening on socket: " + e.getMessage());
        }
      }
    };
    Thread t = new Thread(r, "Socket listener");
    t.setDaemon(true);
    t.start();

  }

  public boolean isClosed() {
    return serverSocket == null || serverSocket.isClosed();
  }

  public LogImporter getLogImporter() {
    return logImporter;
  }

  public int getPort() {
    return port;
  }
}
