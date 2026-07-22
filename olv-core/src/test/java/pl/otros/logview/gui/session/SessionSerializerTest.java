package pl.otros.logview.gui.session;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

public class SessionSerializerTest {

  private List<FileToOpen> singleFilesToOpen = Collections.singletonList(new FileToOpen("file:///tmp/a.txt", OpenMode.FROM_END, Level.FINEST, Optional.of("logImporter")));
  private List<FileToOpen> twoFilesToOpen = Arrays.asList(
    new FileToOpen("file:///tmp/a.txt", OpenMode.FROM_END, Level.FINEST, Optional.of("logImporter1")),
    new FileToOpen("file:///tmp/B.txt", OpenMode.FROM_START, Level.INFO, Optional.of("logImporter2"))
  );


  @DataProvider(name = "serialize")
  public Object[][] dataProvider() throws IOException {
    return new Object[][]{
      new Object[]{
        "empty",
        Collections.emptyList(),
        "empty.json" },
      new Object[]{
        "1 session with 1 file",
        Collections.singletonList(new Session("session1", singleFilesToOpen)),
        "serialised1session1file.json" },
      new Object[]{
        "1 session with 2 files",
        Collections.singletonList(new Session("session2", twoFilesToOpen)),
        "serialised1session2files.json" },
      new Object[]{
        "2 sessions",
        Arrays.asList(
          new Session("session1", singleFilesToOpen),
          new Session("session2", twoFilesToOpen)),
        "serialised2sessions2files.json" }
    };
  }

  @Test(dataProvider = "serialize")
  public void testSerialize(String name, List<Session> sessionList, String jsonFileName) throws Exception {
    String json = IOUtils.toString(this.getClass().getResourceAsStream(jsonFileName), StandardCharsets.UTF_8);
    String serialized = new SessionSerializer().serialize(sessionList);

    // Compare parsed JSON instead of raw strings. Gson ignores formatting and
    // object attribute order. Arrays are sorted by session name because the
    // session order is not relevant for this test.
    Assert.assertEquals(
      getSortedByName(serialized),
      getSortedByName(json));
  }

  /**
   * Parses the JSON array and sorts the sessions by name to allow
   * an order-independent comparison.
   */
  private List<JsonObject> getSortedByName(String json) {
    JsonArray array = JsonParser.parseString(json).getAsJsonArray();

    return StreamSupport.stream(array.spliterator(), false)
      .map(JsonElement::getAsJsonObject)
      .sorted(Comparator.comparing(o -> o.get("name").getAsString()))
      .toList();
  }

}