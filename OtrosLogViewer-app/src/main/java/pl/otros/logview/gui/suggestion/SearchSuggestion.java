package pl.otros.logview.gui.suggestion;

import pl.otros.logview.gui.SubText;

import java.util.Optional;

public class SearchSuggestion {
  private String toDisplay;
  private String fullContent;
  private Optional<SubText> highlightRange = Optional.empty();

  public SearchSuggestion(String toDisplay, String fullContent) {
    this.toDisplay = toDisplay;
    this.fullContent = fullContent;
  }

  public SearchSuggestion(String toDisplay, String fullContent, int highlightStart, int highlightEnd) {
    this(toDisplay, fullContent);
    highlightRange = Optional.of(new SubText(highlightStart, highlightEnd));
  }

  public String getToDisplay() {
    return toDisplay;
  }

  public String getFullContent() {
    return fullContent;
  }

  public Optional<SubText> getHighlightRange() {
    return highlightRange;
  }

  @Override
  public String toString() {
    return "SearchSuggestion{" +
      "toDisplay='" + toDisplay + '\'' +
      ", fullContent='" + fullContent + '\'' +
      ", highlightRange=" + highlightRange +
      '}';
  }
}
