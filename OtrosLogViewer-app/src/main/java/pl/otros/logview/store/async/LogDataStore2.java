package pl.otros.logview.store.async;

import pl.otros.logview.gui.actions.search.SearchResult;
import pl.otros.logview.store.LogDataStore;

public interface LogDataStore2 extends LogDataStore {

  FilterResult filter(LogDataFilter logDataFilter);

  SearchResult search(SearchCriteria searchCriteria);

  int getCountWithoutFilters();
}
