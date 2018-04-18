package pl.otros.logview.gui.firstuse;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import net.miginfocom.swing.MigLayout;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;

public class LookAndFeelPage extends WizardPage {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);


  private final JRadioButton light;
  private final JRadioButton dark;

  LookAndFeelPage() {
    super("Look and Feel", "");
    ImageIcon lightIcon = null;
    ImageIcon darkIcon = null;
    try {
      lightIcon = new ImageIcon(ImageIO.read(FirstTimeUseWizard.class.getResourceAsStream("/theme-thumb-light.png")));
      darkIcon = new ImageIcon(ImageIO.read(FirstTimeUseWizard.class.getResourceAsStream("/theme-thumb-dark.png")));
    } catch (IOException ignore) {
      //
    }
    light = new JRadioButton("Light");
    final JLabel lightLabel = new JLabel(lightIcon);
    lightLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        light.setSelected(true);
      }
    });

    dark = new JRadioButton("Dark");
    final JLabel darkLabel = new JLabel(darkIcon);
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

  public boolean onNext(WizardSettings settings) {
    LookAndFeel lookAndFeel;
    if (light.isSelected()) {
      lookAndFeel = new SubstanceBusinessLookAndFeel();
    } else if (dark.isSelected()) {
      lookAndFeel = new SubstanceGraphiteAquaLookAndFeel();
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
