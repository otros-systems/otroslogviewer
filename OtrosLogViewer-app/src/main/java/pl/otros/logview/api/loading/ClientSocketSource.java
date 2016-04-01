package pl.otros.logview.api.loading;

import java.net.Socket;

public class ClientSocketSource extends Source {

  private Socket socket;

  public ClientSocketSource(Socket socket) {
    this.socket = socket;
  }

  public Socket getSocket() {
    return socket;
  }


  @Override
  public String toString() {
    return "ClientSocketSource{" +
        "socket=" + socket +
        '}';
  }

  @Override
  public String stringForm() {
    return toString();
  }
}
