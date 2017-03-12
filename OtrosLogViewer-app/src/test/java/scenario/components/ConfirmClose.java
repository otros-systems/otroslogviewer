package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.dependency.jsr305.Nonnull;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;

import javax.swing.*;
import java.awt.*;

import static org.assertj.swing.finder.WindowFinder.findDialog;

public class ConfirmClose {
  public static void close(Robot robot) {

    final DialogFixture confirmDialog = findDialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
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
  }
}
