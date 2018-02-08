package pl.otros.logview.gui.session;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SessionDeserializerTest {
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
            "empty.json"},
        new Object[]{
            "1 session with 1 file",
            Collections.singletonList(new Session("session1", singleFilesToOpen)),
            "serialised1session1file.json"},
        new Object[]{
            "1 session with 2 files",
            Collections.singletonList(new Session("session2", twoFilesToOpen)),
            "serialised1session2files.json"},
        new Object[]{
            "2 sessions",
            Arrays.asList(
                new Session("session1", singleFilesToOpen),
                new Session("session2", twoFilesToOpen)),
            "serialised2sessions2files.json"}
    };
  }

  @Test(dataProvider = "serialize")
  public void testDeserialize(String testName, List<Session> sessionList, String jsonFileName) throws Exception {
    String json = IOUtils.toString(this.getClass().getResourceAsStream(jsonFileName), "UTF-8");
    Optional<List<Session>> actual = new SessionDeserializer().deserialize(json);
    System.out.println(actual);
    assertTrue(actual.isPresent(), testName);
    actual.ifPresent(l -> {
      assertThat(l)
          .extracting("name")
          .containsOnly(sessionList.stream().map(Session::getName).toArray());

      for (int i = 0; i < sessionList.size(); i++) {
        assertEquals(l.get(i).getFilesToOpen(), sessionList.get(i).getFilesToOpen(),
            "Files " + l.get(i).getFilesToOpen() + " not equal to " + sessionList.get(i).getFilesToOpen());
      }
    });


  }

}