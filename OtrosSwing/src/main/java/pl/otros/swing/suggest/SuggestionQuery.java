package pl.otros.swing.suggest;

public class SuggestionQuery {
  private String value;
  private int caretLocation;
  private int selectionStart;
  private int selectionEnd;

  public SuggestionQuery(String value, int caretLocation) {
    this(value, caretLocation, -1, -1);
  }

  public SuggestionQuery(String value, int caretLocation, int selectionStart, int selectionEnd) {
    this.value = value;
    this.caretLocation = caretLocation;
    this.selectionStart = selectionStart;
    this.selectionEnd = selectionEnd;
  }

  public String getValue() {
    return value;
  }

  public int getCaretLocation() {
    return caretLocation;
  }

  public int getSelectionStart() {
    return selectionStart;
  }

  public int getSelectionEnd() {
    return selectionEnd;
  }

  @Override
  public String toString() {
    return "SuggestionQuery{" +
      "value='" + value + '\'' +
      ", caretLocation=" + caretLocation +
      ", selectionStart=" + selectionStart +
      ", selectionEnd=" + selectionEnd +
      '}';
  }
}
