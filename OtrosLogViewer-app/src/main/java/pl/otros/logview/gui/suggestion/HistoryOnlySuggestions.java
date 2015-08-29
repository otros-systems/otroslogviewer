package pl.otros.logview.gui.suggestion;

import org.apache.commons.lang.StringUtils;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This implementation will be used instead of Query suggestion source. After implementing
 * Query suggestion, this class can be remobed
 */
public class HistoryOnlySuggestions implements SuggestionSource<SearchSuggestion> {

  final List<String> history;

  public HistoryOnlySuggestions(List<String> history) {
    this.history = history;
  }

  @Override
  public List<SearchSuggestion> getSuggestions(String s) {

    return history.stream()
      .filter(x-> StringUtils.containsIgnoreCase(x,s))
      .map(x->new SearchSuggestion(x,x))
      .collect(Collectors.toList());
  }
}
