package pl.otros.swing;

import java.util.Optional;

public class Progress implements Comparable<Progress>{
  private int value = 0;
  private int min = 0;
  private int max = 100;
  private Optional<String> message;


  public Progress(int value, int max) {
    this.value = value;
    this.max = max;
  }

  public Progress(int value, int max, String message) {
    this.value = value;
    this.max = max;
    this.message = Optional.ofNullable(message);
  }

  public Progress(int value, int max, int min, Optional<String> message) {
    this.min = min;
    this.max = max;
    this.value = value;
    this.message = message;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }

  public int getValue() {
    return value;
  }

  public Optional<String> getMessage() {
    return message;
  }

  @Override
  public int compareTo(Progress o) {
    return value - o.getValue();
  }
}
