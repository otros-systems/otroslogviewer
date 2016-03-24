package pl.otros.logview.gui.suggestion;

import pl.otros.logview.gui.actions.search.SearchAction;
import pl.otros.logview.api.services.PersistService;
import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.ArrayList;
import java.util.List;

public class PersistedSuggestionSource implements SuggestionSource<SearchSuggestion> {

  public static final String KEY = "searchHistory";
  private PersistService persistService;
  private SearchSuggestionSource decorate;

  public PersistedSuggestionSource(SearchSuggestionSource decorate, PersistService persistService) {
    this.persistService = persistService;
    this.decorate = decorate;
    decorate.setHistory(load());
  }

  public void addHistory(SearchAction.SearchMode searchMode, String query) {
    final List<SearchHistory> history = decorate.getHistory();
    final SearchHistory item = new SearchHistory(searchMode, query);
    if (history.contains(item)) {
      history.remove(item);
    }
    history.add(0,item);
    decorate.setHistory(history);
    persist(history);
  }

  protected void setHistory(List<SearchHistory> history) {
    decorate.setHistory(history);
    persist(history);
  }

  private void persist(List<SearchHistory> history){
    try {
      persistService.persist(KEY,history);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private List<SearchHistory> load() {
    return persistService.load(KEY,new ArrayList<>());
  }

  public void setSearchMode(SearchAction.SearchMode searchMode) {
    decorate.setSearchMode(searchMode);
  }

  @Override
  public List<SearchSuggestion> getSuggestions(SuggestionQuery query) {
    return decorate.getSuggestions(query);
  }
}
