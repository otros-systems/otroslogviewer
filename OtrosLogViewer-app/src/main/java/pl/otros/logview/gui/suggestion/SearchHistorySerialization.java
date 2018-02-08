package pl.otros.logview.gui.suggestion;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.services.Deserializer;
import pl.otros.logview.api.services.Serializer;
import pl.otros.logview.gui.actions.search.SearchAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class SearchHistorySerialization {
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchHistorySerialization.class.getName());

  private final Serializer<List<SearchHistory>, String> serializer;
  private final Deserializer<ArrayList<SearchHistory>, String> deserializer;
  private static final String SEARCH_MODE = "searchMode";
  private static final String QUERY = "query";

  SearchHistorySerialization() {

    serializer = value -> {
      final List<JSONObject> collect = value.stream().map(s -> {
        final HashMap<String, String> map = new HashMap<>();
        map.put(SEARCH_MODE, s.getSearchMode().name());
        map.put(QUERY, s.getQuery());
        return new JSONObject(map);
      }).collect(Collectors.toList());
      final JSONArray jsonArray = new JSONArray(collect);
      return jsonArray.toString();
    };

    deserializer = data -> {
      try {
        JSONArray a = new JSONArray(data);
        final int length = a.length();
        final ArrayList<SearchHistory> searchHistories = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
          try {
            final JSONObject o = a.getJSONObject(i);
            final String searchMde = o.getString(SEARCH_MODE);
            final String query = o.getString(QUERY);
            searchHistories.add(new SearchHistory(SearchAction.SearchMode.valueOf(searchMde), query));
          } catch (Exception e) {
            LOGGER.warn("Cant parse JSON object, ignoring it", e);
          }
        }
        return Optional.of(searchHistories);
      } catch (JSONException e) {
        LOGGER.error("Can't parse data: " + data, e);
      }
      return Optional.empty();
    };
  }


  Serializer<List<SearchHistory>, String> serializer(){
    return serializer;
  }

  Deserializer<ArrayList<SearchHistory>, String> deserializer() {
    return deserializer;
  }
}
