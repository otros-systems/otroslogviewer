package scenario;

import org.awaitility.Duration;
import org.fest.util.Files;
import org.testng.annotations.Test;
import scenario.components.LogViewPanel;
import scenario.components.MainFrame;
import scenario.components.ParseClipboardDialog;
import scenario.testng.RetryAnalyzer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.stream.IntStream;

import static org.assertj.swing.assertions.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ParseClipboardTest extends OtrosLogViewerBaseTest {


  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void pasteClipboardOnOpen() throws IOException {
    final File file1 = File.createTempFile("otrosTest", "");
    logEvents(file1, 10);
    final String clipboardContent = Files.contentOf(file1, "UTF-8");
    setClipboard(clipboardContent);
    final MainFrame mainFrame = new MainFrame(robot());
    final ParseClipboardDialog dialog = mainFrame.welcomeScreen().clickParseClipboard();
    final String actual = dialog.clipboardTextAreaContent().text();
    assertThat(actual).isEqualTo(clipboardContent);
  }

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void processClipboardWithUnixCommand() {
    setClipboard("line1\nline2\nline3");
    final MainFrame mainFrame = new MainFrame(robot());
    final ParseClipboardDialog dialog = mainFrame.welcomeScreen().clickParseClipboard();

    dialog.processingPattern().setText("sed s/line/entry/g | grep 1 | cut -c 5-6");

    await().ignoreExceptions().until(() -> dialog.processedContent().text().equals("y1"));

  }

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void importLogsFromClipboard() throws IOException {
    final File tempFile = File.createTempFile("olv", "logs");
    logEvents(tempFile, 10, integer -> Level.INFO);
    final String logsInClipboard = Files.contentOf(tempFile, "UTF-8").trim();

    final MainFrame mainFrame = new MainFrame(robot());
    final ParseClipboardDialog dialog = mainFrame.welcomeScreen().clickParseClipboard();

    setClipboard(logsInClipboard);
    dialog.refresh().click();

    dialog.waitForProcessedContent(logsInClipboard);
    assertThat(dialog.processedContent().text()).isEqualTo(logsInClipboard);

    dialog.processingPattern().setText("sed s/Message/msg/g");

    dialog.waitForProcessedContent(logsInClipboard.replaceAll("msg", "msg"));

    final LogViewPanel logViewPanel = dialog.importLogs();

    await("waiting for 10 events in log table")
      .atMost(Duration.ONE_MINUTE)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 10);
    IntStream.range(0, 9)
      .forEach(i -> logViewPanel.logsTable().hasValueInRow(i, "msg " + i));

  }

  private void setClipboard(String s) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(new Transferable() {
      @Override
      public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.stringFlavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.stringFlavor);
      }

      @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.stringFlavor)) {
          return s;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    }, (clipboard1, contents) -> {
    });
  }
}
