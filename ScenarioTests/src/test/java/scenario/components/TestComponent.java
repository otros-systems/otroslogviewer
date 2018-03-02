package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.AbstractComponentFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static org.awaitility.Awaitility.await;

public abstract class TestComponent<T extends AbstractComponentFixture, U extends TestComponent> {

  protected final Robot robot;

  protected TestComponent(Robot robot) {
    this.robot = robot;
  }

  public abstract T me();

  @SuppressWarnings("unchecked")
  public final U waitFor() {
    await().until(this::isVisible);
    return (U) this;
  }

  @NotNull
  public final Boolean isVisible() {
    try {
      me().focus();
      return true;
    } catch (Exception e){
      return false;
    }
  }

  public void clickButton(JButtonFixture button){
    await().until(button::isEnabled);
    button.click();
  }

  public GenericTypeMatcher<JButton> matcherForButtonWithText(String text){
    return new GenericTypeMatcher<JButton>(JButton.class) {
      @Override
      protected boolean isMatching(JButton component) {
        return component.getText().equalsIgnoreCase(text);
      }
    };
  }
}
