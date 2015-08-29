package pl.otros.logview.gui.suggestion;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class RegexSearchSuggestionSourceTest {

  private RegexSearchSuggestionSource underTest = new RegexSearchSuggestionSource(new ArrayList<>());

  @DataProvider(name = "data")
  public Object[][] suggestionsDataProvider() {
    return new Object[][]{
      new Object[]{"[abc]{","2}\n2,}\n2,4}"},
      new Object[]{"[","abc]\n" +
        "^abc]\n" +
        "a-zA-Z]\n" +
        "a-dm-p]\n" +
        "a-z&&[def]]\n" +
        "a-z&&[^bc]]"},
      new Object[]{"as",""},
      new Object[]{"[a-z]","*\n" +
        "?\n" +
        "+\n" +
        "{n}\n" +
        "{n,}\n" +
        "{n,m}\n" +
        "*?\n" +
        "??\n" +
        "+?\n" +
        "{n}?\n" +
        "{n,}?\n" +
        "{n,m}?\n" +
        "*+\n" +
        "?+\n" +
        "++\n" +
        "{n}+\n" +
        "{n,}+\n" +
        "{n,m}+"},
      new Object[]{"[\\w\\d]","*\n" +
        "?\n" +
        "+\n" +
        "{n}\n" +
        "{n,}\n" +
        "{n,m}\n" +
        "*?\n" +
        "??\n" +
        "+?\n" +
        "{n}?\n" +
        "{n,}?\n" +
        "{n,m}?\n" +
        "*+\n" +
        "?+\n" +
        "++\n" +
        "{n}+\n" +
        "{n,}+\n" +
        "{n,m}+"},
      new Object[]{"\\w","*\n" +
        "?\n" +
        "+\n" +
        "{n}\n" +
        "{n,}\n" +
        "{n,m}\n" +
        "*?\n" +
        "??\n" +
        "+?\n" +
        "{n}?\n" +
        "{n,}?\n" +
        "{n,m}?\n" +
        "*+\n" +
        "?+\n" +
        "++\n" +
        "{n}+\n" +
        "{n,}+\n" +
        "{n,m}+"},
      new Object[]{"\\d","*\n" +
        "?\n" +
        "+\n" +
        "{n}\n" +
        "{n,}\n" +
        "{n,m}\n" +
        "*?\n" +
        "??\n" +
        "+?\n" +
        "{n}?\n" +
        "{n,}?\n" +
        "{n,m}?\n" +
        "*+\n" +
        "?+\n" +
        "++\n" +
        "{n}+\n" +
        "{n,}+\n" +
        "{n,m}+"},
      new Object[]{"\\s","*\n" +
        "?\n" +
        "+\n" +
        "{n}\n" +
        "{n,}\n" +
        "{n,m}\n" +
        "*?\n" +
        "??\n" +
        "+?\n" +
        "{n}?\n" +
        "{n,}?\n" +
        "{n,m}?\n" +
        "*+\n" +
        "?+\n" +
        "++\n" +
        "{n}+\n" +
        "{n,}+\n" +
        "{n,m}+"},
      new Object[]{"\\","d\n" +
        "D\n" +
        "w\n" +
        "W\n" +
        "s\n" +
        "S"}
    };


  }

  @Test(dataProvider = "data")
  public void testGetSuggestions(String text,String suggestionsNewLined) throws Exception {
    final List<SearchSuggestion> suggestions = underTest.getSuggestions(text);
    final String result = suggestions.stream().map(s -> s.getFullContent().substring(text.length())).collect(Collectors.joining("\n"));
    assertEquals(result, suggestionsNewLined);
  }

  @Test
  public void testShowHistorySuggestions(){
    //given
    final List<String> history = Arrays.asList("aa", "bab", "cfs");
    final RegexSearchSuggestionSource underTest = new RegexSearchSuggestionSource(history);

    //when
    final List<SearchSuggestion> suggestions = underTest.getSuggestions("a");

    //then
    assertEquals(suggestions.stream().map(SearchSuggestion::getFullContent).collect(Collectors.joining(",")),"aa,bab");
  }
}