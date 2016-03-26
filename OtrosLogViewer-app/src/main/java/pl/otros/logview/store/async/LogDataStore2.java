package pl.otros.logview.store.async;

import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.gui.actions.search.SearchResult;

public interface LogDataStore2 extends LogDataStore {

  FilterResult filter(LogDataFilter logDataFilter);

  SearchResult search(SearchCriteria searchCriteria);

  int getCountWithoutFilters();
}
