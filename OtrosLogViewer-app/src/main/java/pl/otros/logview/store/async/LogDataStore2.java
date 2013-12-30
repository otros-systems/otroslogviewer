package pl.otros.logview.store.async;

import pl.otros.logview.gui.actions.search.SearchResult;
import pl.otros.logview.store.LogDataStore;

public interface LogDataStore2 extends LogDataStore {

  public FilterResult filter(LogDataFilter logDataFilter);

  public SearchResult search(SearchCriteria searchCriteria);

  public int getCountWithoutFilters();
}
