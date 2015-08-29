package pl.otros.logview.gui.suggestion;

import pl.otros.swing.suggest.SuggestionSource;

import java.util.List;
import java.util.stream.Collectors;

public class StringSearchSuggestionSource implements SuggestionSource<SearchSuggestion> {

  protected List<String> history;

  public StringSearchSuggestionSource(List<String> history) {
    this.history = history;
  }

  @Override
  public List<SearchSuggestion> getSuggestions(String queryString) {
    return historySuggestions(queryString);
  }

  protected List<SearchSuggestion> historySuggestions(String queryString) {
    final String lowerCase = queryString.toLowerCase();
    return history.stream()
      .filter(h -> h.toLowerCase().contains(lowerCase))
      .map(h -> new SearchSuggestion(h, h, h.toLowerCase().indexOf(lowerCase), h.toLowerCase().indexOf(lowerCase) + lowerCase.length()))
      .collect(Collectors.toList());
  }
}
