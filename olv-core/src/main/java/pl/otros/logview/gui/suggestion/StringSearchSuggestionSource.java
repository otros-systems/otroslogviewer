package pl.otros.logview.gui.suggestion;

import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.List;
import java.util.stream.Collectors;

public class StringSearchSuggestionSource implements SuggestionSource<SearchSuggestion> {

  protected List<SuggestionQuery> history;

  public StringSearchSuggestionSource(List<SuggestionQuery> history) {
    this.history = history;
  }

  @Override
  public List<SearchSuggestion> getSuggestions(SuggestionQuery queryString) {
    return historySuggestions(queryString);
  }

  protected List<SearchSuggestion> historySuggestions(SuggestionQuery queryString) {
    final String lowerCase = queryString.getValue().toLowerCase();
    return history.stream()
      .map(SuggestionQuery::getValue)
      .filter(h -> h.toLowerCase().contains(lowerCase))
      .map(h -> new SearchSuggestion(h, h, h.toLowerCase().indexOf(lowerCase), h.toLowerCase().indexOf(lowerCase) + lowerCase.length()))
      .collect(Collectors.toList());
  }
}
