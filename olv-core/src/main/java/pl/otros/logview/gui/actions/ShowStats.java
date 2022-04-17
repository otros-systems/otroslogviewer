package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.services.StatsService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class ShowStats extends OtrosAction {

  private final JTextArea message;
  private final JScrollPane scrollPane;

  public ShowStats(OtrosApplication otrosApplication) {
    super("Show my usage statistics", Icons.DOCUMENT_NUMBER, otrosApplication);
    message = new JTextArea("");
    message.setFont(new Font(Font.MONOSPACED, Font.PLAIN, message.getFont().getSize()));
    message.setEditable(false);

    scrollPane = new JScrollPane(message);
    scrollPane.setBorder(BorderFactory.createTitledBorder("OtrosLogViewer statistics"));
  }

  @Override
  protected void actionPerformedHook(ActionEvent e) {
    final StatsService statsService = getOtrosApplication().getServices().getStatsService();
    final Map<String, Long> stats = statsService.getStats();
    final OptionalInt max = stats.keySet().stream().mapToInt(String::length).max();
    String content = stats
      .entrySet()
      .stream()
      .sorted(Comparator.comparing(Map.Entry::getKey))
      .map(entry -> String.format("%1$-" + max.orElse(0) + "s", entry.getKey()) + "=" + entry.getValue())
      .collect(Collectors.joining("\n"));
    message.setText(content);
    final JTabbedPane jTabbedPane = getOtrosApplication().getJTabbedPane();
    final boolean alreadyOpen = Arrays
      .stream(jTabbedPane.getComponents())
      .anyMatch(c -> c == scrollPane);

    if (alreadyOpen) {
      jTabbedPane.setSelectedComponent(scrollPane);
    } else {
      getOtrosApplication().addClosableTab("Statistics", "Statistics", Icons.DOCUMENT_NUMBER, scrollPane, true);
    }


  }
}
