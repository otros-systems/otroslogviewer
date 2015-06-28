package pl.otros.logview.gui.suggestion;

import com.google.common.base.Joiner;
import pl.otros.logview.accept.query.org.apache.log4j.suggestion.QuerySuggestionSource;
import pl.otros.logview.gui.actions.search.SearchAction;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionSource implements SuggestionSource<SearchSuggestion> {

  private SearchAction.SearchMode searchMode;
  private List<String> history;

  public SearchSuggestionSource(SearchAction.SearchMode searchMode, List<String> history) {
    this.searchMode = searchMode;
    this.history = history;
  }

  public void setModeAndHistory(SearchAction.SearchMode searchMode, List<String> history){

    this.searchMode = searchMode;
    this.history = history;
  }

  @Override
  public List<SearchSuggestion> getSuggestions(String s) {
    SuggestionSource<SearchSuggestion> source;
    if (searchMode.equals(SearchAction.SearchMode.STRING_CONTAINS)){
      source = new StringSearchSuggestionSource(history);
    } else if (searchMode.equals(SearchAction.SearchMode.REGEX)){
      source = new RegexSearchSuggestionSource(history);
    } else if (searchMode.equals(SearchAction.SearchMode.QUERY)){
      source = new QuerySuggestionSource(history);
    } else {
      source = new StringSearchSuggestionSource(new ArrayList<>(0));
    }
    return source.getSuggestions(s);
  }
}
