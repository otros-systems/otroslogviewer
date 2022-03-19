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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SearchSuggestion that = (SearchSuggestion) o;

    if (toDisplay != null ? !toDisplay.equals(that.toDisplay) : that.toDisplay != null) return false;
    if (fullContent != null ? !fullContent.equals(that.fullContent) : that.fullContent != null) return false;
    return highlightRange != null ? highlightRange.equals(that.highlightRange) : that.highlightRange == null;

  }

  @Override
  public int hashCode() {
    int result = toDisplay != null ? toDisplay.hashCode() : 0;
    result = 31 * result + (fullContent != null ? fullContent.hashCode() : 0);
    result = 31 * result + (highlightRange != null ? highlightRange.hashCode() : 0);
    return result;
  }
}
