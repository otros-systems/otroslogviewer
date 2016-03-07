package pl.otros.logview.gui.config;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.pagosoft.plaf.PgsLookAndFeel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.pushingpixels.substance.api.skin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.actions.FontSize;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Appearance extends AbstractConfigView implements InMainConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(Appearance.class.getName());

  private final JPanel panel;
  private final OtrosApplication otrosApplication;
  private final SpinnerNumberModel model;
  private final JComboBox<UIManager.LookAndFeelInfo> lookAndFeelInfoJComboBox;
  private final JCheckBox customFontSize;
  private final JSpinner fontSize;

  public Appearance(OtrosApplication otrosApplication) {
    super("appearance", "Appearance", "Appearance, font size, look and feel");
    this.otrosApplication = otrosApplication;
    panel = new JPanel(new MigLayout("left"));
    model = new SpinnerNumberModel(12, 8, 30, 1);

    lookAndFeelInfoJComboBox = new JComboBox<>(getLookAndFeels());
    final DefaultListCellRenderer renderer = new MyDefaultListCellRenderer();
    lookAndFeelInfoJComboBox.setRenderer(renderer);
    fontSize = new JSpinner(model);

    customFontSize = new JCheckBox();
    customFontSize.addItemListener(e -> fontSize.setEnabled(customFontSize.isSelected()));

    addLabel("Custom font size", 'c', customFontSize, panel);
    addLabel("Font size", 'f', fontSize, panel);
    addLabel("Look and feel \u2B51", 'l', lookAndFeelInfoJComboBox, panel);
    panel.add(new JLabel("\u2B51Requires restart"), "growx, span, wrap");
  }

  private UIManager.LookAndFeelInfo[] getLookAndFeels() {
    final List<UIManager.LookAndFeelInfo> installed = Arrays.asList(UIManager.getInstalledLookAndFeels());
    final List<BasicLookAndFeel> plastic = Arrays.asList(new PlasticXPLookAndFeel(),
      new Plastic3DLookAndFeel(),
      new PlasticLookAndFeel(),
      new PgsLookAndFeel());

    final List<BasicLookAndFeel> substance = Arrays.asList(
      new SubstanceAutumnLookAndFeel(),
      new SubstanceBusinessBlackSteelLookAndFeel(),
      new SubstanceBusinessBlueSteelLookAndFeel(),
      new SubstanceBusinessLookAndFeel(),
      new SubstanceCeruleanLookAndFeel(),
      new SubstanceChallengerDeepLookAndFeel(),
      new SubstanceCremeCoffeeLookAndFeel(),
      new SubstanceCremeLookAndFeel(),
      new SubstanceDustCoffeeLookAndFeel(),
      new SubstanceDustLookAndFeel(),
      new SubstanceEmeraldDuskLookAndFeel(),
      new SubstanceGeminiLookAndFeel(),
      new SubstanceGraphiteAquaLookAndFeel(),
      new SubstanceGraphiteGlassLookAndFeel(),
      new SubstanceGraphiteLookAndFeel(),
      new SubstanceMagellanLookAndFeel(),
      new SubstanceMarinerLookAndFeel(),
      new SubstanceMistAquaLookAndFeel(),
      new SubstanceMistSilverLookAndFeel(),
      new SubstanceModerateLookAndFeel(),
      new SubstanceNebulaBrickWallLookAndFeel(),
      new SubstanceNebulaLookAndFeel(),
      new SubstanceOfficeBlack2007LookAndFeel(),
      new SubstanceOfficeBlue2007LookAndFeel(),
      new SubstanceOfficeSilver2007LookAndFeel(),
      new SubstanceRavenLookAndFeel(),
      new SubstanceSaharaLookAndFeel(),
      new SubstanceTwilightLookAndFeel()
    );

    final List<UIManager.LookAndFeelInfo> extraLf = Stream.concat(plastic.stream(),substance.stream())
      .map(l -> new UIManager.LookAndFeelInfo(l.getName(), l.getClass().getName()))
      .collect(Collectors.toList());
    final ArrayList<UIManager.LookAndFeelInfo> result = new ArrayList<>();
    result.addAll(installed);
    result.addAll(extraLf);
    return result.toArray(new UIManager.LookAndFeelInfo[result.size()]);
  }


  @Override
  public JComponent getView() {
    return panel;
  }

  @Override
  public ValidationResult validate() {
    return new ValidationResult();
  }

  @Override
  public void loadConfiguration(Configuration c) {
    final int defaultValue = fontSize.getFont().getSize();
    model.setValue(c.getInt("appearance.fontSize", defaultValue));
    customFontSize.setSelected(c.getBoolean("appearance.customFontSize", false));
    fontSize.setEnabled(customFontSize.isSelected());

    final String currentLf = UIManager.getLookAndFeel().getClass().getName();
    final String lookAndFeel = c.getString("appearance.lookAndFeel", currentLf);
    for (int i = 0; i < lookAndFeelInfoJComboBox.getItemCount(); i++) {
      if (lookAndFeelInfoJComboBox.getItemAt(i).getClassName().equals(lookAndFeel)) {
        lookAndFeelInfoJComboBox.setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(ConfKeys.APPEARANCE_FONT_SIZE, model.getValue());
    final UIManager.LookAndFeelInfo selectedLf = lookAndFeelInfoJComboBox.getModel().getElementAt(lookAndFeelInfoJComboBox.getSelectedIndex());
    c.setProperty(ConfKeys.APPEARANCE_LOOK_AND_FEEL, selectedLf.getClassName());
    c.setProperty(ConfKeys.APPEARANCE_CUSTOM_FONT_SIZE, customFontSize.isSelected());
  }

  @Override
  public void apply() {
    SwingUtilities.invokeLater(() -> new FontSize(otrosApplication, (Integer) model.getValue()).actionPerformed(null));
  }

  private static class MyDefaultListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      final UIManager.LookAndFeelInfo feelInfo = (UIManager.LookAndFeelInfo) value;
      ((JLabel) component).setText(feelInfo.getName());
      try {
        LOGGER.info("Creating look and feel " + feelInfo.getName() + "/" + feelInfo.getClassName() );
        final LookAndFeel lf = (LookAndFeel) Class.forName(feelInfo.getClassName()).newInstance();
        component.setBackground(lf.getDefaults().getColor(isSelected ? "ComboBox.selectionBackground" : "Label.background"));
        component.setForeground(lf.getDefaults().getColor(isSelected ? "ComboBox.selectionForeground" : "Label.foreground"));
        component.setFont(lf.getDefaults().getFont("Label.font"));
      } catch (Throwable e) {
        LOGGER.error("Can't get look and feel properties for "  + feelInfo.getName() + "/" + feelInfo.getClassName(),e);
      }
      if (isSelected) {
        ((JLabel) component).setBorder(BorderFactory.createLineBorder(Color.BLACK));
      } else {
        ((JLabel) component).setBorder(null);
      }
      return component;
    }
  }
}
