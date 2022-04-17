package scenario;

import org.testng.annotations.Test;
import scenario.components.LogViewPanel;
import scenario.components.MainFrame;
import scenario.testng.RetryAnalyzer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static org.awaitility.Awaitility.await;

public class ImportFromSocketTest extends OtrosLogViewerBaseTest {

  private final int port = 9999;
  private final String logs =
    "2012-01-23 22:26:01 c.g.a a\n" +
      "INFO: Message 0\n" +
      "2012-01-23 22:26:02 h.f.g.e a\n" +
      "INFO: Message 1\n" +
      "2012-01-23 22:26:02 e.d.d.f.g.f.e a\n" +
      "INFO: Message 2\n" +
      "2012-01-23 22:26:02 b.c a";

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void testImportFromSocketListener() throws IOException {
    final MainFrame mainFrame = new MainFrame(robot());
    final LogViewPanel logViewPanel = mainFrame
      .welcomeScreen()
      .clickStartSocketListener()
      .setLogParser("JUL simple formatter")
      .setPort(port)
      .startListening();

    final Socket socket = sendLogsOnSocket(port, logs);
    await().until(() -> logViewPanel.logsTable().visibleLogsCount() > 1);
    socket.close();
  }

  private Socket sendLogsOnSocket(int port, String logs) throws IOException {
    final Socket localhost = new Socket("localhost", port);
    final OutputStream outputStream = localhost.getOutputStream();
    outputStream.write(logs.getBytes());
    outputStream.flush();
    return localhost;
  }
}
