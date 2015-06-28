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
  public List<SearchSuggestion> getSuggestions(String s) {
    final String sLc = s.toLowerCase();
    return history.stream()
      .filter(h -> h.toLowerCase().contains(sLc))
      .map(h -> new SearchSuggestion(h, h, h.toLowerCase().indexOf(sLc), h.toLowerCase().indexOf(sLc)+s.length()))
      .collect(Collectors.toList());
  }
}
