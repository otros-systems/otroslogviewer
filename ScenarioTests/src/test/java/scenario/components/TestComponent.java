package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.AbstractComponentFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.jetbrains.annotations.NotNull;

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
}
