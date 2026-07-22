package pl.otros.logview.gui.suggestion;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.services.Deserializer;
import pl.otros.logview.api.services.Serializer;
import pl.otros.logview.gui.actions.search.SearchAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SearchHistorySerialization {
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchHistorySerialization.class.getName());

  private final Serializer<List<SearchHistory>, String> serializer;
  private final Deserializer<ArrayList<SearchHistory>, String> deserializer;
  private static final String SEARCH_MODE = "searchMode";
  private static final String QUERY = "query";

  SearchHistorySerialization() {

    serializer = value -> {
      final JsonArray jsonArray = new JsonArray();
      value.forEach(s -> {
        final JsonObject o = new JsonObject();
        o.addProperty(SEARCH_MODE, s.getSearchMode().name());
        o.addProperty(QUERY, s.getQuery());
        jsonArray.add(o);
      });
      return jsonArray.toString();
    };

    deserializer = data -> {
      try {
        final JsonElement jsonElement = JsonParser.parseString(data);
        if (!jsonElement.isJsonArray()) {
          return Optional.empty();
        }
        final JsonArray a = jsonElement.getAsJsonArray();
        final int size = a.size();
        final ArrayList<SearchHistory> searchHistories = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
          try {
            final JsonObject o = a.get(i).getAsJsonObject();
            final String searchMde = o.get(SEARCH_MODE).getAsString();
            final String query = o.get(QUERY).getAsString();
            searchHistories.add(new SearchHistory(SearchAction.SearchMode.valueOf(searchMde), query));
          } catch (Exception e) {
            LOGGER.warn("Cant parse JSON object, ignoring it", e);
          }
        }
        return Optional.of(searchHistories);
      } catch (JsonSyntaxException | IllegalStateException e) {
        LOGGER.error("Can't parse data: " + data, e);
      }
      return Optional.empty();
    };
  }


  Serializer<List<SearchHistory>, String> serializer() {
    return serializer;
  }

  Deserializer<ArrayList<SearchHistory>, String> deserializer() {
    return deserializer;
  }
}
