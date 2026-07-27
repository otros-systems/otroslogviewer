package pl.otros.logview.gui.firstuse;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.gui.util.LookAndFeelUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class LookAndFeelPage extends JPanel implements FirstUsePage {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);


  private final JRadioButton light;
  private final JRadioButton dark;

  LookAndFeelPage() {
    light = new JRadioButton("Light");
    final JLabel lightLabel = new JLabel(Icons.THEME_THUMB_LIGHT);
    lightLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        light.setSelected(true);
      }
    });

    dark = new JRadioButton("Dark");
    final JLabel darkLabel = new JLabel(Icons.THEME_THUMB_DARK);
    darkLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        dark.setSelected(true);
      }
    });

    light.setSelected(true);
    final ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(light);
    buttonGroup.add(dark);
    this.setLayout(new MigLayout("center"));
    this.add(light, "center");
    this.add(dark, "center, wrap");
    this.add(lightLabel, "gapx 40");
    this.add(darkLabel, "gapx 40");
  }

  @Override
  public String getTitle() {
    return "Look and Feel";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public JComponent getView() {
    return this;
  }

  @Override
  public boolean onNext(WizardContext settings) {
    LookAndFeel lookAndFeel;
    if (light.isSelected()) {
      lookAndFeel = LookAndFeelUtil.getLightLookAndFeel();
    } else if (dark.isSelected()) {
      lookAndFeel = LookAndFeelUtil.getDarkLookAndFeel();
    } else {
      return true;
    }
    settings.put(Config.LOOK_AND_FEEL, lookAndFeel.getClass().getName());
    try {
      UIManager.setLookAndFeel(lookAndFeel);
      Arrays
        .stream(Window.getWindows())
        .forEach(SwingUtilities::updateComponentTreeUI);
    } catch (UnsupportedLookAndFeelException ignore) {
      LOGGER.error("Can't set Look and Feel", ignore);
    }
    return true;
  }
}
