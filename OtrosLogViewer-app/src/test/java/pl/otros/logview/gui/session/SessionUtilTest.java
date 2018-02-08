package pl.otros.logview.gui.session;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SessionUtilTest {

  @Test
  public void testGroupFilesByServer() throws Exception {
    //given
    final ArrayList<FileToOpen> filesToOpen = new ArrayList<>();
    filesToOpen.add(new FileToOpen("sftp://server1.pl/asd/s", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server2.pl/asd/a", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server2.pl/asd/d", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server1.pl/asd/v", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("file:///asd/v", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server3.pl/asd/n", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    Session session = new Session("A", filesToOpen);

    //when
    final Map<String, List<FileToOpen>> map = SessionUtil.groupFilesByServer(session);

    //then
    assertEquals(map.size(),4);
    assertTrue(map.containsKey("local file"));
    assertTrue(map.containsKey("server1.pl"));
    assertTrue(map.containsKey("server2.pl"));
    assertTrue(map.containsKey("server3.pl"));
  }

  @Test
  public void testGroupFilesByServerEmpty() throws Exception {

    //given
    final ArrayList<FileToOpen> filesToOpen = new ArrayList<>();
    Session session = new Session("A", filesToOpen);

    //when
    final Map<String, List<FileToOpen>> actual = SessionUtil.groupFilesByServer(session);

    //then
    assertEquals(actual, Collections.emptyMap());
  }

  @Test
  public void testToStringGroupedByServer() throws Exception {
    //given
    final ArrayList<FileToOpen> filesToOpen = new ArrayList<>();
    filesToOpen.add(new FileToOpen("sftp://server1.pl/asd/s", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server2.pl/asd/a", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server2.pl/asd/d", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server1.pl/asd/v", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("file:///asd/v", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server3/asd/n", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    Session session = new Session("A", filesToOpen);
    String expected = "local file:\n" +
        "  file:///asd/v\n" +
        "\n" +
        "server1.pl:\n" +
        "  sftp://server1.pl/asd/s\n" +
        "  sftp://server1.pl/asd/v\n" +
        "\n" +
        "server2.pl:\n" +
        "  sftp://server2.pl/asd/a\n" +
        "  sftp://server2.pl/asd/d\n" +
        "\n" +
        "server3:\n" +
        "  sftp://server3/asd/n";

    //when
    final String actual = SessionUtil.toStringGroupedByServer(session);

    //then
    assertEquals(actual, expected);
  }

  @Test
  public void testToStringGroupedByServerEmpty() throws Exception {
    //given
    final ArrayList<FileToOpen> filesToOpen = new ArrayList<>();
    Session session = new Session("A", filesToOpen);

    //when
    final String actual = SessionUtil.toStringGroupedByServer(session);

    //then
    assertEquals(actual, "");
  }

}