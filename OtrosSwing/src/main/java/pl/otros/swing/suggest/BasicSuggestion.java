package pl.otros.swing.suggest;



public class BasicSuggestion implements Comparable<BasicSuggestion> {

  private String toDisplay;
  private String toInsert;

  public BasicSuggestion(String toDisplay, String toInsert) {
    this.toDisplay = toDisplay;
    this.toInsert = toInsert;
  }

  public String getToDisplay() {
    return toDisplay;
  }

  public String getToInsert() {
    return toInsert;
  }

  @Override
  public String toString() {
    return "BasicSuggestion{" +
      "toDisplay='" + toDisplay + '\'' +
      ", toInsert='" + toInsert + '\'' +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BasicSuggestion that = (BasicSuggestion) o;
    return !(toDisplay != null ? !toDisplay.equals(that.toDisplay) : that.toDisplay != null) && !(toInsert != null ? !toInsert.equals(that.toInsert) : that.toInsert != null);

  }

  @Override
  public int hashCode() {
    int result = toDisplay != null ? toDisplay.hashCode() : 0;
    result = 31 * result + (toInsert != null ? toInsert.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(BasicSuggestion o) {
    return toDisplay.compareTo(o.toDisplay);
  }
}
