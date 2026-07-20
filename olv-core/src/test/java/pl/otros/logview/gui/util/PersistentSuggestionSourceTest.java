package pl.otros.logview.gui.util;

import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import pl.otros.logview.api.services.Deserializer;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.Serializer;
import pl.otros.swing.suggest.SuggestionQuery;

import java.util.*;
import java.util.function.Function;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class PersistentSuggestionSourceTest {

    private PersistService persistService;
    private PersistentSuggestionSource<Integer> suggestionSource;

    private static class MemoryPersistService implements PersistService {

        Map<String, String> map = new HashMap();

        @Override
        public <T> void persist(String key, T o, Serializer<T, String> serializer) throws Exception {
            map.put(key, serializer.serialize(o));
        }

        @Override
        public <T> T load(String key, T defaultValue, Deserializer<T, String> deserializer) {
            return deserializer.deserialize(map.get(key)).get();
        }
    }

    @BeforeTest
    public void before() {
        persistService = new MemoryPersistService();
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
        persistService.persist("key", "1111\n1112\n3232", s -> s);

        //when
        final List<Integer> suggestions = suggestionSource.getSuggestions(new SuggestionQuery("11", 0));

        //then
        assertEquals(suggestions.size(), 2);
        assertEquals(suggestions, Arrays.asList(1111, 1112));
    }

    @Test
    public void testAdd() throws Exception {
        //given
        persistService.persist("key", "1111\n1112\n3232", s -> s);

        //when
        suggestionSource.add(1134, 5454);
        final List<Integer> suggestions = suggestionSource.getSuggestions(new SuggestionQuery("11", 0));

        //then
        String value = persistService.load("key", "", Optional::of);
        assertEquals(value, "1134\r\n5454\r\n1111\r\n1112\r\n3232\r\n");
    }

}