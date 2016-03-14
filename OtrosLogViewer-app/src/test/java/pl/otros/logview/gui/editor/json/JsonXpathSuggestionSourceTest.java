package pl.otros.logview.gui.editor.json;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.parser.json.JsonExtractor;
import pl.otros.swing.suggest.BasicSuggestion;
import pl.otros.swing.suggest.SuggestionQuery;

import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class JsonXpathSuggestionSourceTest {

  private final List<String> allKeys = new ArrayList<>(Arrays.asList(JsonExtractor.KEYS));

  @BeforeClass
  public void beforeClass() {
    allKeys.add("name");
    allKeys.add("type");
    allKeys.add("description");
  }

  final HashSet<String> xpaths = new HashSet<>(Arrays.asList("ala", "aba", "all"));


  @DataProvider(name = "suggestions")
  public Object[][] dataProvider() {
    String text = "level=prop.level\n" + //17
      "\n" + //18
      "logger=prop.logger\n" + //37
      "message=prop.message";
    final ArrayList<String> notUsedKeys = new ArrayList<>(allKeys);
    notUsedKeys.removeAll(Arrays.asList("message", "logger", "level"));

    List<String> xpaths = Arrays.asList(
      "prop.level",
      "prop.logger",
      "message",
      "prop.host",
      "prop.line");

    return new Object[][]{

      new Object[]{"Empty Line", text, xpaths, 17, notUsedKeys.stream().map(s -> new BasicSuggestion(s, s+"=")).sorted().collect(Collectors.toList())},

      new Object[]{"Key after 1 letter", text, xpaths, 38, Arrays.asList(
        new BasicSuggestion("markerColor","arkerColor="),
        new BasicSuggestion("mdcKeys","dcKeys="),
        new BasicSuggestion("method","ethod=")
    )},

      new Object[]{"Cursor at start", text, xpaths, 0,
        notUsedKeys.stream().map(s -> new BasicSuggestion(s, s+"=")).sorted().collect(Collectors.toList())
      },
      new Object[]{"After =", text, xpaths, 6,
        xpaths.stream().map(s -> new BasicSuggestion(s, s)).sorted().collect(Collectors.toList())
      },
      new Object[]{"With first letter of value", text, xpaths, 7,
        Arrays.asList(
          new BasicSuggestion("prop.host", "rop.host"),
          new BasicSuggestion("prop.level", "rop.level"),
          new BasicSuggestion("prop.line", "rop.line"),
          new BasicSuggestion("prop.logger", "rop.logger"))
      },
      new Object[]{"No result", "aaaaaa", xpaths, 3, Collections.emptyList()},
    };
  }

  @Test(dataProvider = "suggestions")
  public void testGetSuggestions(String name,
                                 String text,
                                 Collection<String> xpaths,
                                 int caretPosition,
                                 List<BasicSuggestion> expectedSuggestions
  ) throws Exception {
//given
    final JsonXpathSuggestionSource underTest = new JsonXpathSuggestionSource();
    underTest.setJsonPaths(new HashSet<>(xpaths));

    //when
    final List<BasicSuggestion> suggestions = underTest.getSuggestions(new SuggestionQuery(text, caretPosition));

    //then
    assertEquals(suggestions, expectedSuggestions);
  }


  @DataProvider(name = "keySuggestion")
  public Object[][] dataProviderKeySuggestion() {
    return new Object[][]{
      new Object[]{"level=asdf", 0, allKeys.stream().map(s -> new BasicSuggestion(s, s+"=")).sorted().collect(Collectors.toList())},
      new Object[]{"level=asdf", 1, Arrays.asList(
        new BasicSuggestion("level", "evel="),
        new BasicSuggestion("line", "ine="),
        new BasicSuggestion("logger", "ogger="))
      }
    };
  }

  @Test(dataProvider = "keySuggestion")
  public void testGetKeySuggestions(String line, int position, List<String> expected) {
    //given
    final JsonXpathSuggestionSource underTest = new JsonXpathSuggestionSource();
    underTest.setJsonPaths(xpaths);

    //when
    final List<BasicSuggestion> result = underTest.getKeysSuggestion(line.substring(0,position), new HashSet<>());

    //then
    assertEquals(result, expected);
  }


  @DataProvider(name = "suggestionForTypeJson")
  public static Object[][] suggestionsForTypeJsonDataProvider() {
    return new Object[][]{
      new Object[]{"type=json", 5, Collections.singletonList(new BasicSuggestion("json", "json"))},
      new Object[]{"type=json", 6, Collections.singletonList(new BasicSuggestion("json", "son"))},
      new Object[]{"type=json", 7, Collections.singletonList(new BasicSuggestion("json", "on"))},
      new Object[]{"type=json", 8, Collections.singletonList(new BasicSuggestion("json", "n"))},
      new Object[]{"type=json", 9, Collections.singletonList(new BasicSuggestion("json", ""))},
      new Object[]{"type = json", 7, Collections.singletonList(new BasicSuggestion("json", "json"))},
      new Object[]{"type = json", 8, Collections.singletonList(new BasicSuggestion("json", "son"))},
      new Object[]{"type = json", 9, Collections.singletonList(new BasicSuggestion("json", "on"))},
      new Object[]{"type = json", 10, Collections.singletonList(new BasicSuggestion("json", "n"))},
      new Object[]{"type = json", 11, Collections.singletonList(new BasicSuggestion("json", ""))}
    };
  }


  @Test(dataProvider = "suggestionForTypeJson")
  public void suggestionsForTypeJson(String line, int position, List<BasicSuggestion> expected) {
    //given
    final JsonXpathSuggestionSource underTest = new JsonXpathSuggestionSource();
    underTest.setJsonPaths(xpaths);

    //when
    final List<BasicSuggestion> result = underTest.suggestionsForTypeJson(line, position);

    //then
    assertEquals(result, expected);
  }


  @DataProvider(name = "suggestionForMdc")
  public static Object[][] suggestionForMdcDataProvider() {
    return new Object[][]{

      new Object[]{"one option", "mdcKeys=ab", 10,
        Collections.singletonList(new BasicSuggestion("aba", "a, "))},

      new Object[]{"3 options", "mdcKeys=a", 9,
        Arrays.asList(
          new BasicSuggestion("aba", "ba, "),
          new BasicSuggestion("ala", "la, "),
          new BasicSuggestion("all", "ll, ")
        )},

      new Object[]{"empty key", "mdcKeys=", 8,
        Arrays.asList(
          new BasicSuggestion("aba", "aba, "),
          new BasicSuggestion("ala", "ala, "),
          new BasicSuggestion("all", "all, ")
        )},

      // -----------------------------------01234567890
      new Object[]{"rest allKeys after coma", "mdcKeys=aba,", 12,
        Arrays.asList(
          new BasicSuggestion("ala", "ala, "),
          new BasicSuggestion("all", "all, ")
        )},

      new Object[]{"suggest coma", "mdcKeys=aba,ala", 15,
        Collections.singletonList(new BasicSuggestion(", ", ", "))},


      new Object[]{"second key", "mdcKeys=aba,al", 14,
        Arrays.asList(
          new BasicSuggestion("ala", "a, "),
          new BasicSuggestion("all", "l, ")
        )},

      new Object[]{"second key, with spaces", "mdcKeys=aba , al", 16, Arrays.asList(
        new BasicSuggestion("ala", "a, "),
        new BasicSuggestion("all", "l, ")
      )},
    };
  }

  @Test(dataProvider = "suggestionForMdc")
  public void suggestionForMdc(String name, String line, int position, List<BasicSuggestion> expected) {
    //given
    final HashSet<String> xpaths = new HashSet<>(Arrays.asList("ala", "aba", "all"));
    final JsonXpathSuggestionSource underTest = new JsonXpathSuggestionSource();
    underTest.setJsonPaths(xpaths);

    //when
    final List<BasicSuggestion> result = underTest.suggestionsForMdc(line, position);

    //then
    assertEquals(result, expected);
  }


  @DataProvider(name = "lineForPosition")
  public Object[][] lineForPositionProvider() {
    final String text =
      "ala=asf\n" +
        "\n" +
        "asa=sdf\n" +
        "sdfa=sdf";

    final String textCrLf =
      "ala=asf\r\n" +
        "asa=sdf\r\n" +
        "sdfa=sdf\r\n" +
        "\r\n" +
        "a=c";

    return new Object[][]{
      new Object[]{text, 0, "ala=asf"},
      new Object[]{text, 4, "ala=asf"},
      new Object[]{text, 8, ""},
      new Object[]{text, 9, "asa=sdf"},
      new Object[]{text, 18, "sdfa=sdf"},
      new Object[]{text, text.length(), "sdfa=sdf"},
      new Object[]{"", 0, ""},

      new Object[]{textCrLf, 0, "ala=asf"},
      new Object[]{textCrLf, 4, "ala=asf"},
      new Object[]{textCrLf, 9, "asa=sdf"},
      new Object[]{textCrLf, 18, "sdfa=sdf"},
      new Object[]{textCrLf, textCrLf.length(), "a=c"},
      new Object[]{"", 0, ""},
      new Object[]{textCrLf, textCrLf.length() - 3, "a=c"},
      new Object[]{textCrLf, textCrLf.length() - 2, "a=c"},
    };
  }


  @Test(dataProvider = "lineForPosition")
  public void testLineForPosition(String text, int position, String expected) {
    //given
    final JsonXpathSuggestionSource jsonXpathSuggestionSource = new JsonXpathSuggestionSource();
    //when
    final String line = jsonXpathSuggestionSource.lineForPosition(text, position);

    //then
    assertEquals(line, expected);
  }
}