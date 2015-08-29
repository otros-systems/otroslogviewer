package pl.otros.logview.gui.suggestion;

import pl.otros.logview.gui.actions.search.SearchAction;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchSuggestionSource implements SuggestionSource<SearchSuggestion> {

  private SearchAction.SearchMode searchMode;
  private List<SearchHistory> history;

  public SearchSuggestionSource(SearchAction.SearchMode searchMode) {
    this(searchMode,new ArrayList<>());
  }

  public SearchSuggestionSource(SearchAction.SearchMode searchMode, List<SearchHistory> history) {
    this.searchMode = searchMode;
    this.history = history;
  }

  public void setSearchMode(SearchAction.SearchMode searchMode) {
    this.searchMode = searchMode;
  }

  @Override
  public List<SearchSuggestion> getSuggestions(String s) {
    SuggestionSource<SearchSuggestion> source;
    final List<String> historyForMode = getHistory()
      .stream()
      .filter(h -> h.getSearchMode().equals(searchMode))
      .map(SearchHistory::getQuery)
      .collect(Collectors.toList());
    if (searchMode.equals(SearchAction.SearchMode.STRING_CONTAINS)) {
      source = new StringSearchSuggestionSource(historyForMode);
    } else if (searchMode.equals(SearchAction.SearchMode.REGEX)) {
      source = new RegexSearchSuggestionSource(historyForMode);
    } else if (searchMode.equals(SearchAction.SearchMode.QUERY)) {
      //TODO implement query suggestion
      source = new HistoryOnlySuggestions(historyForMode);
    } else {
      source = new StringSearchSuggestionSource(new ArrayList<>(0));
    }
    return source.getSuggestions(s);
  }


  public void addHistory(SearchAction.SearchMode searchMode, String query) {
    history.add(new SearchHistory(searchMode,query));
  }

  public SearchAction.SearchMode getSearchMode() {
    return searchMode;
  }

  protected List<SearchHistory> getHistory() {
    return history;
  }

  protected void setHistory(List<SearchHistory> history) {
    this.history = history;
  }
}
