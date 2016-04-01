package pl.otros.logview.api.loading;

import java.net.Socket;

public class SocketSource extends Source {

  private Socket socket;

  public SocketSource(Socket socket) {
    this.socket = socket;
  }

  public Socket getSocket() {
    return socket;
  }

  @Override
  public String stringForm() {
    return "Socket";
  }
}
