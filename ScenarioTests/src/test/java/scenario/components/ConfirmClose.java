package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.dependency.jsr305.Nonnull;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class ConfirmClose {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmClose.class);

  public static void close(Robot robot) {
    LOGGER.info("Closing confirmation dialog");
    final DialogFixture confirmDialog = WindowFinder.findDialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
      @Override
      protected boolean isMatching(@Nonnull Dialog component) {
        return component.getTitle().equals("Are you sure?");
      }
    }).using(robot);

    final JButtonFixture ok = confirmDialog.button(new GenericTypeMatcher<JButton>(JButton.class) {
      @Override
      protected boolean isMatching(@Nonnull JButton component) {
        return component.getText().equals("OK");
      }
    });
    ok.click();

    await()
      .atMost(30, TimeUnit.SECONDS)
      .ignoreExceptions()
      .until(confirmDialog::requireNotVisible);

    LOGGER.info("Confirmation dialog is closed");
  }
}
