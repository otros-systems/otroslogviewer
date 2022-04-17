package pl.otros.logview.accept;

import pl.otros.logview.api.model.LogData;

import java.util.logging.Level;

public class LevelHigherOrEqualAcceptCondition extends AbstractAcceptContidion {

  private Level threshold;

  public LevelHigherOrEqualAcceptCondition(Level threshold) {
    this.threshold = threshold;
  }

  @Override
  public boolean accept(LogData data) {
    return data.getLevel() == null || data.getLevel().intValue() >= threshold.intValue();
  }
}
