package pl.otros.logview.gui.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SessionUtil {

  static Map<String, List<FileToOpen>> groupFilesByServer(Session session) {
    return session.getFilesToOpen()
        .stream()
        .collect(Collectors.groupingBy(FileToOpen::host));
  }

  public static String toStringGroupedByServer(Session session) {
    final Map<String, List<FileToOpen>> map = groupFilesByServer(session);
    return map.keySet()
        .stream()
        .sorted()
        .map(server -> String.format("%s:%n%s%n", server, map.get(server)
            .stream()
            .map(FileToOpen::getUri)
            .map(s -> "  " + s)
            .collect(Collectors.joining("\n"))))
        .collect(Collectors.joining("\n")).trim();
  }


  public static void main(String[] args) {
    final ArrayList<FileToOpen> filesToOpen = new ArrayList<>();
    filesToOpen.add(new FileToOpen("sftp://server1.pl/asd/s", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server2.pl/asd/a", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server2.pl/asd/d", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server1.pl/asd/v", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("file:///asd/v", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    filesToOpen.add(new FileToOpen("sftp://server3/asd/n", OpenMode.FROM_END, Level.FINEST, Optional.empty()));
    Session session = new Session("A", filesToOpen);
    System.out.print(toStringGroupedByServer(session) + ".");

  }

}
