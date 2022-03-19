package pl.otros.swing.suggest;

import javax.swing.text.JTextComponent;

public class SuggestionResult<T> {
  private T value;
  private SuggestionQuery suggestionSource;
  private JTextComponent textComponent;

  public SuggestionResult(T value, SuggestionQuery suggestionSource, JTextComponent textComponent) {
    this.value = value;
    this.suggestionSource = suggestionSource;
    this.textComponent = textComponent;
  }

  public T getValue() {
    return value;
  }

  public SuggestionQuery getSuggestionSource() {
    return suggestionSource;
  }

  public JTextComponent getTextComponent() {
    return textComponent;
  }
}
