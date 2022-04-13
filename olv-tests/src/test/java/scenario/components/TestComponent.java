package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.AbstractComponentFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class TestComponent<T extends AbstractComponentFixture, U extends TestComponent> {

  private static final Logger LOGGER = getLogger(TestComponent.class);
  protected final Robot robot;

  protected TestComponent(Robot robot) {
    this.robot = robot;
  }

  public abstract T me();

  @SuppressWarnings("unchecked")
  public final U waitFor() {
    try {
      await()
        .atMost(30, TimeUnit.SECONDS)
        .until(this::isVisible);
    } catch (Exception e) {
      LOGGER.error("Component is not visible: ", e);
      LOGGER.error("Component is not visible, cause:", e.getCause());
      throw e;
    }
    return (U) this;
  }

  @Nonnull
  public final Boolean isVisible() {
    try {
      me().focus();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public void clickButton(JButtonFixture button) {
    await().until(button::isEnabled);
    button.click();
  }

  public GenericTypeMatcher<JButton> matcherForButtonWithText(String text) {
    return new GenericTypeMatcher<JButton>(JButton.class) {
      @Override
      protected boolean isMatching(JButton component) {
        return component.getText().equalsIgnoreCase(text);
      }
    };
  }
}
