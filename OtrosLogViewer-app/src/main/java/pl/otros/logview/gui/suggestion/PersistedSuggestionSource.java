package pl.otros.logview.gui.suggestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.services.Deserializer;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.Serializer;
import pl.otros.logview.gui.actions.search.SearchAction;
import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.ArrayList;
import java.util.List;

public class PersistedSuggestionSource implements SuggestionSource<SearchSuggestion> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PersistedSuggestionSource.class.getName());

  private static final String KEY = "searchHistory";

  private PersistService persistService;
  private SearchSuggestionSource decorate;
  private final Serializer<List<SearchHistory>, String> serlizer;
  private final Deserializer<ArrayList<SearchHistory>, String> deserialiser;

  public PersistedSuggestionSource(SearchSuggestionSource decorate, PersistService persistService) {
    this.persistService = persistService;
    this.decorate = decorate;
    final SearchHistorySerialization searchHistorySerialization = new SearchHistorySerialization();
    serlizer = searchHistorySerialization.serializer();
    deserialiser = searchHistorySerialization.deserializer();
    decorate.setHistory(load());
  }

  public void addHistory(SearchAction.SearchMode searchMode, String query) {
    final List<SearchHistory> history = decorate.getHistory();
    final SearchHistory item = new SearchHistory(searchMode, query);
    if (history.contains(item)) {
      history.remove(item);
    }
    history.add(0, item);
    decorate.setHistory(history);
    persist(history);
  }

  private void persist(List<SearchHistory> history) {
    try {
      persistService.persist(KEY, history, serlizer);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private List<SearchHistory> load() {
    return persistService.load(KEY, new ArrayList<>(), deserialiser);
  }

  public void setSearchMode(SearchAction.SearchMode searchMode) {
    decorate.setSearchMode(searchMode);
  }

  @Override
  public List<SearchSuggestion> getSuggestions(SuggestionQuery query) {
    return decorate.getSuggestions(query);
  }
}
