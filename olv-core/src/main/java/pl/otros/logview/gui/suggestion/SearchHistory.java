package pl.otros.logview.gui.suggestion;

import pl.otros.logview.gui.actions.search.SearchAction;

public class SearchHistory {
  private SearchAction.SearchMode searchMode;
  private String query;

  public SearchHistory(SearchAction.SearchMode searchMode, String query) {
    this.searchMode = searchMode;
    this.query = query;
  }

  public SearchAction.SearchMode getSearchMode() {
    return searchMode;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SearchHistory that = (SearchHistory) o;

    return searchMode == that.searchMode && !(query != null ? !query.equals(that.query) : that.query != null);
  }

  @Override
  public int hashCode() {
    int result = searchMode != null ? searchMode.hashCode() : 0;
    result = 31 * result + (query != null ? query.hashCode() : 0);
    return result;
  }
}
