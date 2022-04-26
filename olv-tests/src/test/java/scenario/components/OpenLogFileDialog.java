package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.core.Robot;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.dependency.jsr305.Nonnull;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;

import static org.awaitility.Awaitility.await;

public class OpenLogFileDialog extends TestComponent<DialogFixture, OpenLogFileDialog> {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenLogFileDialog.class);

  private final FrameFixture frame;

  public OpenLogFileDialog(FrameFixture frameFixture, Robot robot) {
    super(robot);
    this.frame = frameFixture;
  }

  @Override
  public DialogFixture me() {
    return frame.dialog("VfsBrowserDialog");
  }

  public LogViewPanel openFile(File file) {
    final DialogFixture vfsBrowserDialog = WindowFinder.findDialog("VfsBrowserDialog").using(robot);
    LOGGER.info("Open file " + file.getAbsolutePath());

    vfsBrowserDialog.textBox("VfsBrowser.filter").setText("");
    JTextComponentFixture pathTextBox = vfsBrowserDialog.textBox("VfsBrowser.path");
    pathTextBox.click();
    pathTextBox.setText("file://" + file.getParentFile().getAbsolutePath());
    pathTextBox.pressAndReleaseKey(KeyPressInfo.keyCode('\n'));

    final JTableFixture table = vfsBrowserDialog.table(new GenericTypeMatcher<JTable>(JTable.class) {
      @Override
      protected boolean isMatching(@Nonnull JTable component) {
        LOGGER.info("Checking: {} {}", component.getName(), component.getRowCount());
        return true;
      }
    });

    final String fileName = file.getName();
    LOGGER.info(" opening file " + fileName);
    vfsBrowserDialog.textBox("VfsBrowser.filter").setText(fileName);
    vfsBrowserDialog.button("VfsBrowser.refresh").click();
    await().ignoreExceptions().until(() -> table.cell(TableCell.row(1).column(0)).click());
    vfsBrowserDialog.button("VfsBrowser.open").click();
    LOGGER.info(" file " + file.getAbsolutePath() + " opened");

    return new LogViewPanel(frame, robot);
  }


}
