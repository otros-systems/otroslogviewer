package pl.otros.logview.gui.suggestion;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.gui.actions.search.SearchAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SearchHistorySerializationTest {


  @DataProvider(name = "toSerializerData")
  public Object[][] dataProvider() {
    return new Object[][]{
        new Object[]{new SearchHistory(SearchAction.SearchMode.STRING_CONTAINS, "string search"), "[{\"query\":\"string search\",\"searchMode\":\"STRING_CONTAINS\"}]"},
        new Object[]{new SearchHistory(SearchAction.SearchMode.REGEX, "regex search"), "[{\"query\":\"regex search\",\"searchMode\":\"REGEX\"}]"},
        new Object[]{new SearchHistory(SearchAction.SearchMode.QUERY, "query search"), "[{\"query\":\"query search\",\"searchMode\":\"QUERY\"}]"}
    };
  }

  @Test(dataProvider = "toSerializerData")
  public void testSerializer(SearchHistory searchHistory, String expected) throws Exception {
    //Given
    //When
    final String serialized = new SearchHistorySerialization().serializer().serialize(Collections.singletonList(searchHistory));

    //Then
    assertEquals(serialized,expected);
  }

  @Test
  public void testDeserializer() throws Exception {
    String data = "[\n" +
        "  {\n" +
        "    \"query\": \"s+\",\n" +
        "    \"searchMode\": \"REGEX\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"query\": \"text\",\n" +
        "    \"searchMode\": \"STRING_CONTAINS\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"query\": \"level>INFO\",\n" +
        "    \"searchMode\": \"QUERY\"\n" +
        "  }\n" +
        "]";
    final Optional<ArrayList<SearchHistory>> deserialize = new SearchHistorySerialization().deserializer().deserialize(data);
    assertTrue(deserialize.isPresent());
    deserialize.ifPresent(list -> {
      assertEquals(list.get(0).getQuery(), "s+");
      assertEquals(list.get(0).getSearchMode(), SearchAction.SearchMode.REGEX);
      assertEquals(list.get(1).getQuery(), "text");
      assertEquals(list.get(1).getSearchMode(), SearchAction.SearchMode.STRING_CONTAINS);
      assertEquals(list.get(2).getQuery(), "level>INFO");
      assertEquals(list.get(2).getSearchMode(), SearchAction.SearchMode.QUERY);
    });
  }

}