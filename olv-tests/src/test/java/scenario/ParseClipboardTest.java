package scenario;

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
import java.nio.file.Files;
import java.time.Duration;
import java.util.logging.Level;
import java.util.stream.IntStream;

import static org.assertj.swing.assertions.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ParseClipboardTest extends OtrosLogViewerBaseTest {


  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void pasteClipboardOnOpen() throws IOException {
    final File file1 = File.createTempFile("otrosTest", "");
    logEvents(file1, 10);
    final String clipboardContent = Files.readString(file1.toPath());
    setClipboard(clipboardContent);
    final MainFrame mainFrame = new MainFrame(robot());
    final ParseClipboardDialog dialog = mainFrame.welcomeScreen().clickParseClipboard();
    final String actual = dialog.clipboardTextAreaContent().text();
    assertThat(actual).isEqualTo(clipboardContent);
  }

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void importLogsFromClipboard() throws Exception {
    final File tempFile = File.createTempFile("olv", "logs");
    logEvents(tempFile, 10, integer -> Level.INFO);
    final String logsInClipboard = Files.readString(tempFile.toPath()).trim();

    final MainFrame mainFrame = new MainFrame(robot());
    final ParseClipboardDialog dialog = mainFrame.welcomeScreen().clickParseClipboard();

    setClipboard(logsInClipboard);
    dialog.pasteClipboard();

    await("waiting for log importer detection")
      .atMost(Duration.ofSeconds(10L))
      .until(() -> dialog.clipboardTextAreaContent().text().equals(logsInClipboard));

    await("waiting for import button to be enabled")
      .atMost(Duration.ofSeconds(10L))
      .until(dialog::isImportEnabled);

    final LogViewPanel logViewPanel = dialog.importLogs();

    await("waiting for 10 events in log table")
      .atMost(Duration.ofMinutes(1L))
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 10);
    IntStream.range(0, 9)
      .forEach(i -> logViewPanel.logsTable().hasValueInRow(i, "Message " + i));

  }

  private void setClipboard(String s) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(new Transferable() {
      @Override
      public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{ DataFlavor.stringFlavor };
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
