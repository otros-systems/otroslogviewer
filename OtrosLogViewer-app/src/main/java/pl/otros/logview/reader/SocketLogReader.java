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
package pl.otros.logview.reader;

import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.parser.ParsingContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketLogReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(SocketLogReader.class.getName());
  private ServerSocket serverSocket;
  private final StatusObserver observer;
  private final LogDataCollector logDataCollector;
  private final LogImporter logImporter;
  private final int port;

  public SocketLogReader(LogImporter logImporter, LogDataCollector logDataCollector, StatusObserver observer, int port) {
    super();
    this.logImporter = logImporter;
    this.logDataCollector = logDataCollector;
    this.observer = observer;
    this.port = port;
  }

  public void close() throws IOException {
    if (serverSocket != null) {
      serverSocket.close();
      serverSocket = null;
    }
  }

  public void start() throws Exception {
    serverSocket = new ServerSocket(port);
    Runnable r = () -> {
      try {
        while (true) {
          Socket s = serverSocket.accept();
          SocketHandler handler = new SocketHandler(s);
          Thread t = new Thread(handler, "Socket handler: " + s.getInetAddress() + ":" + s.getPort());
          t.setDaemon(true);
          t.start();
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

  private class SocketHandler implements Runnable {

    public Socket socket;

    public SocketHandler(Socket socket) {
      super();
      this.socket = socket;
    }

    @Override
    public void run() {
      String adress = socket.getInetAddress().toString() + ":" + socket.getPort();

      try {
        InputStream in = socket.getInputStream();
        ParsingContext parsingContext = new ParsingContext(adress, adress);
        logImporter.initParsingContext(parsingContext);
        logImporter.importLogs(in, logDataCollector, parsingContext);
        observer.updateStatus(adress + " - connection finished ");
      } catch (IOException e) {
        e.printStackTrace();
        observer.updateStatus(adress + " - connection broken: " + e.getMessage(), StatusObserver.LEVEL_ERROR);
      } catch (Exception e) {
        e.printStackTrace();
        observer.updateStatus("Can't initialize log parser", StatusObserver.LEVEL_ERROR);
      }
    }

  }

  public LogImporter getLogImporter() {
    return logImporter;
  }

  public int getPort() {
    return port;
  }
}
