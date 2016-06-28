package pl.otros.logview.gui.util;

import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import pl.otros.logview.api.services.PersistService;
import pl.otros.swing.suggest.SuggestionQuery;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class PersistentSuggestionSourceTest {

  private PersistService persistService;
  private PersistentSuggestionSource<Integer> suggestionSource;

  @BeforeTest
  public void before() {
    persistService = Mockito.mock(PersistService.class);
    suggestionSource = new PersistentSuggestionSource<>("key",
      persistService,
      (i, q) -> i.toString().contains(q.getValue()),
      Object::toString,
      Integer::valueOf
    );
  }

  @Test
  public void testGetSuggestions() throws Exception {
    //given
    when(persistService.load("key", "")).thenReturn("1111\n1112\n3232");

    //when
    final List<Integer> suggestions = suggestionSource.getSuggestions(new SuggestionQuery("11", 0));

    //than
    assertEquals(suggestions.size(), 2);
    assertEquals(suggestions, Arrays.asList(1111, 1112));
  }

  @Test
  public void testAdd() throws Exception {
    //given
    when(persistService.load("key", "")).thenReturn("1111\n1112\n3232");

    //when
    suggestionSource.add(1134, 5454);
    final List<Integer> suggestions = suggestionSource.getSuggestions(new SuggestionQuery("11", 0));

    //than
    verify(persistService,times(1)).persist("key", "1134\r\n5454\r\n1111\r\n1112\r\n3232\r\n");
  }

}