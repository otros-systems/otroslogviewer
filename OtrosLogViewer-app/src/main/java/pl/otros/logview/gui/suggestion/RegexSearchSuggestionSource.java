package pl.otros.logview.gui.suggestion;


import java.util.List;

public class RegexSearchSuggestionSource extends StringSearchSuggestionSource {


  public RegexSearchSuggestionSource(List<String> history) {
    super(history);
  }

  @Override
  public List<SearchSuggestion> getSuggestions(String s) {
    //currently returns only recent search. Add helpers to regex like:
    // some\d  => * zero or more
    //         => + one or more
    return super.getSuggestions(s);
  }
}
