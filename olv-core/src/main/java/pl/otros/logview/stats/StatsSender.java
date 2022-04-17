package pl.otros.logview.stats;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.jdesktop.swingx.JXHyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.services.Services;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static pl.otros.logview.api.ConfKeys.*;

public class StatsSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatsSender.class);

  public void maybeSendStats(Window parent, Configuration configuration, Services services, String olvVersion) {
    if (configuration.getBoolean(SEND_STATS, false)) {
      final long now = System.currentTimeMillis();
      final long interval = 10L * 24 * 60 * 60 * 1000;

      if (!configuration.containsKey(NEXT_STATS_SEND_DATE)) {
        configuration.setProperty(NEXT_STATS_SEND_DATE, now + interval);
      }

      final long nextSendDate = configuration.getLong(NEXT_STATS_SEND_DATE);
      LOGGER.info("Next stats send will occur in: " + new Date(nextSendDate));

      if (now > nextSendDate) {
        configuration.setProperty(NEXT_STATS_SEND_DATE, (now + interval));
        final boolean notifyAboutStats = configuration.getBoolean(ConfKeys.SEND_STATS_NOTIFY, false);
        final Map<String, Long> stats = services.getStatsService().getStats();
        final String uuid = configuration.getString(UUID, "");
        final String javaVersion = System.getProperty("java.version", "");

        if (notifyAboutStats) {
          showDialog(parent,
            stats,
            () -> scheduleSend(services, olvVersion, stats, uuid, javaVersion),
            () -> configuration.setProperty(ConfKeys.SEND_STATS_NOTIFY, false));
        } else {
          scheduleSend(services, olvVersion, stats, uuid, javaVersion);
        }
      }
    }
  }

  @Nonnull
  private ListenableScheduledFuture<?> scheduleSend(Services services, String olvVersion, Map<String, Long> stats, String uuid, String javaVersion) {
    return services
      .getTaskSchedulerService()
      .getListeningScheduledExecutorService()
      .schedule(() -> {
        send(services, olvVersion, stats, uuid, javaVersion);
      }, 2L, TimeUnit.SECONDS);
  }

  private static void showDialog(Window owner, Map<String, Long> stats, Runnable sendFunction, Runnable stopNotifying) {
    JComponent c = createView(stats);
    JDialog dialog = new JDialog(owner, "Sending stats", Dialog.ModalityType.APPLICATION_MODAL);
    final JPanel southPanel = new JPanel(new MigLayout());
    final JButton saveAndClose = new JButton(new AbstractAction("Send and close") {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        dialog.dispose();
        sendFunction.run();
      }
    });
    southPanel.add(saveAndClose);
    southPanel.add(new JButton(new AbstractAction("Send and stop notifying") {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        dialog.dispose();
        sendFunction.run();
        stopNotifying.run();
      }
    }));

    southPanel.add(new JButton(new AbstractAction("Cancel sending") {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        dialog.dispose();
      }
    }));


    final JXHyperlink hyperlink = new JXHyperlink(new AbstractAction("Following stats will be send. Click to see report") {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Desktop.getDesktop().browse(new URI("https://github.com/otros-systems/otroslogviewer-stats"));
        } catch (IOException | URISyntaxException e1) {
          LOGGER.warn("Can't open browser");
        }
      }
    });

    final Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new MigLayout());
    contentPane.add(hyperlink, new CC().dockNorth().gapLeft("10px").gapBottom("10px").gapTop("10px"));
    contentPane.add(southPanel, new CC().dockSouth().pad(10, 10, 10, 10).alignX("center"));
    contentPane.add(c, new CC().growX().gap("10px", "10px", "10px", "10px"));
    dialog.setSize(800, 400);
    dialog.setVisible(true);
    saveAndClose.requestFocusInWindow();
  }

  private static JComponent createView(Map<String, Long> stats) {
    final JPanel jPanel = new JPanel(new BorderLayout());
    final String content = stats
      .entrySet()
      .stream()
      .map(e -> e.getKey() + ": " + e.getValue())
      .collect(Collectors.joining("\n"));
    final JTextArea jTextArea = new JTextArea(content);
    jTextArea.setEditable(false);
    final JScrollPane jScrollPane = new JScrollPane(jTextArea);
    jPanel.add(jScrollPane);

    return jPanel;
  }

  public static void main(String[] args) throws InvocationTargetException, InterruptedException {
    SwingUtilities.invokeAndWait(() -> {
      Map<String, Long> stats = new HashMap<>();
      for (int i = 0; i < 40; i++) {
        stats.put("Action:asdfsdf.sdfsdfsfd.sdfsfd." + i, 10L * i);
      }
      showDialog(null, stats, () -> System.out.println("StatsSender.run sending"), () -> System.out.println("StatsSender.run - disable notify"));
    });
  }


  private void send(Services services, String olvVersion, Map<String, Long> stats, String uuid, String javaVersion) {
    services.getStatsReportService().sendStats(
      stats,
      uuid,
      olvVersion,
      javaVersion
    );
  }
}
