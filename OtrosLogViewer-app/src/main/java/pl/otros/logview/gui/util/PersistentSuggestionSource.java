package pl.otros.logview.gui.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.services.PersistService;
import pl.otros.swing.suggest.SuggestionQuery;
import pl.otros.swing.suggest.SuggestionSource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class PersistentSuggestionSource<T> implements SuggestionSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersistentSuggestionSource.class);

  private String persistKey;
  private PersistService persistService;
  private BiFunction<T, SuggestionQuery, Boolean> filterFunction;
  private Function<T, String> serializer;
  private Function<String, T> deserializer;

  public PersistentSuggestionSource(
    String persistKey,
    PersistService persistService,
    BiFunction<T, SuggestionQuery, Boolean> filterFunction,
    Function<T, String> serializer,
    Function<String, T> deserializer) {

    this.persistKey = persistKey;
    this.persistService = persistService;
    this.filterFunction = filterFunction;
    this.serializer = serializer;
    this.deserializer = deserializer;
  }

  @Override
  public List<T> getSuggestions(SuggestionQuery query) {
    return loadValues().stream()
      .filter(s -> filterFunction.apply(s, query))
      .collect(toList());
  }

  @SafeVarargs
  public final void add(T... values) {
    final List<T> values1 = loadValues();
    values1.addAll(Arrays.asList(values));

    final List<String> collect = values1.stream()
      .map(serializer)
      .map(StringEscapeUtils::escapeCsv)
      .collect(toList());

    try {
      final StringWriter out = new StringWriter();
      CSVFormat.DEFAULT.print(out).printRecords(collect);
      final String content = out.toString();
      persistService.persist(persistKey, content);
    } catch (Exception e) {
      LOGGER.error("Can't store suggestions for " + persistKey, e);
    }
  }

  private List<T> loadValues() {
    try {
      final String load = Optional.ofNullable(persistService.load(persistKey, "")).orElse("");
      Reader in = new StringReader(load);
      return new CSVParser(in, CSVFormat.DEFAULT).getRecords()
        .stream()
        .map(v -> v.get(0))
        .map(deserializer)
        .collect(toList());
    } catch (IOException e) {
      LOGGER.error("Cant read suggestion from persistence for key: " + persistKey);
      return new ArrayList<>();
    }
  }

}
