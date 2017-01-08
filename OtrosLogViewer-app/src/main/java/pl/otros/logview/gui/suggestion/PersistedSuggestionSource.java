package pl.otros.logview.gui.suggestion;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.services.Deserializer;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.Serializer;
import pl.otros.logview.gui.actions.search.SearchAction;
import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PersistedSuggestionSource implements SuggestionSource<SearchSuggestion> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PersistedSuggestionSource.class.getName());

  public static final String KEY = "searchHistory";
  private PersistService persistService;
  private SearchSuggestionSource decorate;
  private final Serializer<List<SearchHistory>, String> serlizer;
  private final Deserializer<ArrayList<SearchHistory>, String> deserialiser;

  public PersistedSuggestionSource(SearchSuggestionSource decorate, PersistService persistService) {
    this.persistService = persistService;
    this.decorate = decorate;
    serlizer = value -> {
      //TODO serialize to JSON
      final List<JSONObject> collect = value.stream().map(s -> {
        final HashMap<String, String> map = new HashMap<>();
        map.put("searchMode", s.getSearchMode().name());
        map.put("query", s.getQuery());
        return new JSONObject(map);
      }).collect(Collectors.toList());
      final JSONArray jsonArray = new JSONArray(collect);
      return jsonArray.toString();
    };
    deserialiser = data -> {
      //TODO deserialize json
      try {
        JSONArray a = new JSONArray(data);
        final int length = a.length();
        //TODO change to IntSream.map
        final ArrayList<SearchHistory> searchHistories = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
          try {
            final JSONObject o = a.getJSONObject(i);
            final String searchMde = o.getString("searchMode");
            final String query = o.getString("query");
            final SearchHistory searchHistory = new SearchHistory(SearchAction.SearchMode.valueOf(searchMde), query);
            searchHistories.add(searchHistory);
          } catch (Exception e) {
            LOGGER.warn("Cant parse JSON object, ignoring it", e);
          }
        }
        return Optional.of(searchHistories);
      } catch (JSONException e) {
        //TODO handle errors
        e.printStackTrace();
      }
      return Optional.empty();
    };
    decorate.setHistory(load());
  }

  public void addHistory(SearchAction.SearchMode searchMode, String query) {
    final List<SearchHistory> history = decorate.getHistory();
    final SearchHistory item = new SearchHistory(searchMode, query);
    if (history.contains(item)) {
      history.remove(item);
    }
    history.add(0, item);
    decorate.setHistory(history);
    persist(history);
  }

  protected void setHistory(List<SearchHistory> history) {
    decorate.setHistory(history);
    persist(history);
  }

  private void persist(List<SearchHistory> history) {
    try {
      persistService.persist(KEY, history, serlizer);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private List<SearchHistory> load() {
    return persistService.load(KEY, new ArrayList<>(), deserialiser);
  }

  public void setSearchMode(SearchAction.SearchMode searchMode) {
    decorate.setSearchMode(searchMode);
  }

  @Override
  public List<SearchSuggestion> getSuggestions(SuggestionQuery query) {
    return decorate.getSuggestions(query);
  }
}
