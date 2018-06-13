package pl.otros.logview.gui.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.jdesktop.swingx.JXHyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.gui.actions.ShowStats;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

public class SendStatsConfigView extends AbstractConfigView implements InMainConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendStatsConfigView.class);

  private static final String DESCRIPTION = "Configuration of sending anonymous statistics";
  private final JCheckBox sendAnonymousStatsData = new JCheckBox("Send anonymous stats data", true);
  private final JCheckBox notifyAboutSending = new JCheckBox("Notify when sending stats", false);
  private final JLabel nextSend = new JLabel();

  private JPanel view;

  public SendStatsConfigView(OtrosApplication otrosApplication) {
    super("sendStats", "Stats sending", DESCRIPTION);
    view = new JPanel(new MigLayout());

    JXHyperlink statsLink = new JXHyperlink(new AbstractAction("Open page with usage report") {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Desktop.getDesktop().browse(new URI("https://github.com/otros-systems/otroslogviewer-stats"));
        } catch (IOException | URISyntaxException e1) {
          LOGGER.warn("Can't open page with stats");
        }
      }
    });


    view.add(sendAnonymousStatsData, "wrap");
    view.add(notifyAboutSending, "wrap");
    view.add(new JXHyperlink(new ShowStats(otrosApplication)), "wrap");
    view.add(statsLink, "wrap");
    view.add(nextSend, "wrap");
  }

  @Override
  public JComponent getView() {
    return view;
  }

  @Override
  public ValidationResult validate() {
    return new ValidationResult();
  }

  @Override
  public void loadConfiguration(Configuration c) {
    sendAnonymousStatsData.setSelected(c.getBoolean(ConfKeys.SEND_STATS, true));
    notifyAboutSending.setSelected(c.getBoolean(ConfKeys.SEND_STATS_NOTIFY, false));
    String date = Optional.
      of(c.getLong(ConfKeys.NEXT_STATS_SEND_DATE, Long.MAX_VALUE))
      .map(ts -> LocalDateTime.ofInstant(new Date(ts).toInstant(), ZoneId.systemDefault()))
      .map(ldt -> ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
      .orElse("?");
    if (c.getBoolean(ConfKeys.SEND_STATS, true)) {
      nextSend.setText("Next send of anonymous data will occur at " + date);
    } else {
      nextSend.setText("Sending statistic data is turned off");
    }
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(ConfKeys.SEND_STATS, sendAnonymousStatsData.isSelected());
    c.setProperty(ConfKeys.SEND_STATS_NOTIFY, notifyAboutSending.isSelected());
  }
}
