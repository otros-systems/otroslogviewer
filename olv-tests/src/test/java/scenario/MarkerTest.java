package scenario;

import org.assertj.swing.data.TableCell;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import pl.otros.logview.api.pluginable.AllPluginables;
import scenario.components.LogViewPanel;
import scenario.components.MainFrame;
import scenario.components.NewMarkerDialog;
import scenario.components.OpenPanel;
import scenario.testng.RetryAnalyzer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MarkerTest extends OtrosLogViewerBaseTest {
  public static final File TEST_MARKER_FILE = new File(AllPluginables.USER_MARKERS, "MarkerTest-useStringMatcherMarker.marker");
  private final String markerFileName = "MarkerTest-createMarker";

  /**
   * Copy marker file before loading the application.
   */
  @BeforeTest
  public static void createMarkerFile() {
    copyResourceFile("MarkerTest-useStringMatcherMarker.marker", TEST_MARKER_FILE);
  }

  @AfterTest
  public void removeMarkerFileAfterTest() {
    if (TEST_MARKER_FILE.exists()) {
      Assert.assertTrue(TEST_MARKER_FILE.delete());
    }
  }


  @AfterMethod
  public void removeMarkerFile() {
    File expectedFile = getMarkerFile();
    if (expectedFile.exists()) {
      Assert.assertTrue(expectedFile.delete());
    }
  }

  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void createStringMatcherMarker() {
    final MainFrame mainFrame = new MainFrame(robot());
    mainFrame.me().menuItemWithPath("Tools", "Show markers editor").click();

    mainFrame.markerListDialog().waitFor().newMarkerButton().click();

    final NewMarkerDialog newMarkerDialog = mainFrame.newMarkerDialog();
    newMarkerDialog.waitFor()
      .setName("My Custom Marker")
      .setDescription("Description of my marker")
      .setGroups("CustomGroup")
      .setStringMatcherCondition("some pattern")
      .save();

    newMarkerDialog.fileChooser().fileNameTextBox().setText(markerFileName);
    newMarkerDialog.fileChooser().approve();

    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> getMarkerFile().exists());
  }


  @Test(retryAnalyzer = RetryAnalyzer.class)
  public void useStringMatcherMarker() throws Exception {
    final MainFrame mainFrame = new MainFrame(robot());

    final File file = File.createTempFile("otrosTest", "");
    file.deleteOnExit();
    Logger logger = logEvents(file, 1);

    final OpenPanel openPanel = mainFrame.welcomeScreen().clickMergeLogs();

    final LogViewPanel logViewPanel = openPanel
      .addFile(file)
      .importFiles();

    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 1);

    logViewPanel.selectGroupComboBox().selectItem("OtherGroup");

    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .untilAsserted(() -> logViewPanel.tailingModeMarkersPanel().requireRowCount(1));

    logViewPanel.tailingModeMarkersPanel().cell(TableCell.row(0).column(0)).click();

    logger.log(Level.WARNING, "This message has some pattern!");
    logger.log(Level.WARNING, "This message has some pattern2!");

    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> logViewPanel.logsTable().visibleLogsCount() == 3);

    Color line1 = logViewPanel.logsTable().me().backgroundAt(TableCell.row(0).column(2)).target();
    Color line3 = logViewPanel.logsTable().me().backgroundAt(TableCell.row(2).column(2)).target();
    Color line2 = logViewPanel.logsTable().me().backgroundAt(TableCell.row(1).column(2)).target();
    Assert.assertEquals(Color.decode("0xE4EAF0"), line1);//Not marked
    Assert.assertEquals(Color.decode("0xE61212"), line2);//red
    Assert.assertEquals(Color.decode("0xFF2B2B"), line3);//red and selected
  }


  private static void copyResourceFile(String fileName, File target) {
    try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
      Assert.assertNotNull(inputStream, "cannot find file in resource folder: " + fileName);
      java.nio.file.Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private File getMarkerFile() {
    return new File(AllPluginables.USER_MARKERS, markerFileName + ".marker");
  }
}
