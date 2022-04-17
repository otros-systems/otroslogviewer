package pl.otros.logview.gui.actions.search;

public interface SearchListener {
  void searchPerformed(SearchAction.SearchMode searchMode, String query);
}
