package pl.otros.logview.gui.suggestion;

import org.apache.commons.lang.StringUtils;
import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This implementation will be used instead of Query suggestion source. After implementing
 * Query suggestion, this class can be remobed
 */
public class HistoryOnlySuggestions implements SuggestionSource<SearchSuggestion> {

  private final List<SuggestionQuery> history;

  public HistoryOnlySuggestions(List<SuggestionQuery> history) {
    this.history = history;
  }

  @Override
  public List<SearchSuggestion> getSuggestions(SuggestionQuery q) {
    String s = q.getValue();
    return history.stream()
      .map(SuggestionQuery::getValue)
      .filter(x -> StringUtils.containsIgnoreCase(x, s))
      .map(x -> new SearchSuggestion(x, x))
      .collect(Collectors.toList());
  }
}
