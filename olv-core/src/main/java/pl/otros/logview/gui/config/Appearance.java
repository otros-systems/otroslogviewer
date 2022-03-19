package pl.otros.logview.gui.config;

import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.jdesktop.swingx.JXHyperlink;
import org.pushingpixels.substance.api.skin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataBuilder;
import pl.otros.logview.api.model.Note;
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.api.theme.ThemeKey;
import pl.otros.logview.gui.ColorIcon;
import pl.otros.logview.gui.actions.FontSize;
import pl.otros.logview.gui.actions.search.SearchAction;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.gui.message.update.FormatMessageDialogWorker;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;
import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;
import pl.otros.swing.rulerbar.RulerBarHelper;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Appearance extends AbstractConfigView implements InMainConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(Appearance.class.getName());

  private final JPanel panel;
  private final OtrosApplication otrosApplication;
  private final SpinnerNumberModel model;
  private final JComboBox<UIManager.LookAndFeelInfo> lookAndFeelInfoJComboBox;
  private final JCheckBox customFontSize;
  private final JSpinner fontSize;
  private final List<ThemeKey> themeKeys;
  private final String soapMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<soap:Envelope \nxmlns:soap=\"http://www.w3.org/2001/12/soap-envelope\"\n" +
    "               soap:encodingStyle=\"http://www.w3.org/2001/12/soap-encoding\">\n" +
    "    <soap:Body xmlns:m=\"http://www.example.org/directions\">\n" +
    "        <m:DirectionsResponse><![CDATA[Cdata content here]]><Train para=\"some argument\">IE122</Train>\n" +
    "        </m:DirectionsResponse>\n" +
    "    </soap:Body><!-- Comment -->" +
    "</soap:Envelope>";
  private final String stacktrace = " Error on serving request\n" +
    "java.io.IOException: Error executing request, connection broken.... :)\n" +
    "  at test.sampleapp.services.hotels.HotelsService.getHotels(HotelsService.java:30)\n" +
    "  at test.sampleapp.SampleApp.performRequests(SampleApp.java:76)\t //target.run()\n" +
    "  at java.lang.Thread.run(Thread.java:745);";
  private final LogData logData = new LogDataBuilder()
    .withDate(new Date())
    .withId(1)
    .withMessage("Something wrong happened when received message.\n" + stacktrace + "\n" + soapMessage)
    .withProperties(Collections.singletonMap("USER_ID", "Stefan"))
    .withLevel(java.util.logging.Level.SEVERE)
    .withMethod("method")
    .withClass("com.company.Logic")
    .withLoggerName("com.company.Logic")
    .withThread("Thread-01-1")
    .withFile("Logic.java")
    .withLineNumber("20")
    .withNote(new Note("Something wrong happened!"))
    .build();
  private final JList<ThemeKey> listOfColorsInSchme;
  private final OtrosJTextWithRulerScrollPane<JTextPane> logDetailsTextPane;

  public Appearance(OtrosApplication otrosApplication) {
    super("appearance", "Appearance", "Appearance, font size, look and feel");
    this.otrosApplication = otrosApplication;

    themeKeys = Arrays.asList(
      ThemeKey.SEARCH_RESULT,
      ThemeKey.LOG_DETAILS_DEFAULT,
      ThemeKey.LOG_DETAILS_MESSAGE,
      ThemeKey.LOG_DETAILS_PROPERTY,
      ThemeKey.LOG_DETAILS_VALUE,
      ThemeKey.LOG_DETAILS_PROPERTY_KEY,
      ThemeKey.LOG_DETAILS_PROPERTY_VALUE,
      ThemeKey.LOG_DETAILS_STACKTRACE_BACKGROUND,
      ThemeKey.LOG_DETAILS_STACKTRACE_FOREGROUND,
      ThemeKey.LOG_DETAILS_STACKTRACE_CLASS,
      ThemeKey.LOG_DETAILS_STACKTRACE_METHOD,
      ThemeKey.LOG_DETAILS_STACKTRACE_FLE,
      ThemeKey.LOG_DETAILS_STACKTRACE_COMMENT,
      ThemeKey.LOG_DETAILS_SOAP_ATTRIBUTE_NAME,
      ThemeKey.LOG_DETAILS_SOAP_ATTRIBUTE_VALUE,
      ThemeKey.LOG_DETAILS_SOAP_CDATA_BACKGROUND,
      ThemeKey.LOG_DETAILS_SOAP_CDATA_FOREGROUND,
      ThemeKey.LOG_DETAILS_SOAP_ELEMENT_NAME,
      ThemeKey.LOG_DETAILS_SOAP_COMMENTS,
      ThemeKey.LOG_DETAILS_SOAP_CONTENT_BACKGROUND,
      ThemeKey.LOG_DETAILS_SOAP_OPERATOR,
      ThemeKey.TEXT_FIELD_ERROR
    );

    panel = new JPanel(new MigLayout(new LC().fill()));
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
    addLabel("Color scheme", new JXHyperlink(new AbstractAction("Reset to defaults") {
      @Override
      public void actionPerformed(ActionEvent e) {
        resetColorsToDefault();
      }
    }), panel);

    final JColorChooser colorChooser = new JColorChooser();
    colorChooser.setBorder(BorderFactory.createTitledBorder("Color chooser"));
    listOfColorsInSchme = new JList<>(themeKeys.toArray(new ThemeKey[0]));
    listOfColorsInSchme.setBorder(BorderFactory.createTitledBorder("Colors"));
    listOfColorsInSchme.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      final Component component = new DefaultListCellRenderer()
        .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (component instanceof JLabel) {
        JLabel jl = (JLabel) component;
        jl.setText(value.getName());
        jl.setIcon(new ColorIcon(otrosApplication.getTheme().getColor(value)));
      }
      return component;
    });
    listOfColorsInSchme.addListSelectionListener(e -> {
      final ThemeKey selectedValue = listOfColorsInSchme.getSelectedValue();
      final Color color = otrosApplication.getTheme().getColor(selectedValue);
      colorChooser.setColor(color);
    });
    final JPanel colorsPanel = new JPanel(new MigLayout());
    logDetailsTextPane = RulerBarHelper.wrapTextComponent(new JTextPane());
    logDetailsTextPane.setBorder(BorderFactory.createTitledBorder("Example log event"));
    logDetailsTextPane.getjTextComponent().setEditable(false);
    colorChooser.getSelectionModel().addChangeListener(e -> {
      final ThemeKey themeKey = listOfColorsInSchme.getSelectedValue();
      if (themeKey != null) {
        otrosApplication.getTheme().setColor(themeKey, colorChooser.getColor());
        listOfColorsInSchme.revalidate();
        listOfColorsInSchme.repaint();
        updateLogDetails();
      }
    });
    colorsPanel.add(listOfColorsInSchme, new CC().alignY("top"));
    colorsPanel.add(colorChooser, new CC().alignY("top"));
    colorsPanel.add(logDetailsTextPane, new CC().spanX().growX());
    updateLogDetails();
    panel.add(colorsPanel, new CC().spanX().wrap());
    addLabel("Default colors", new JXHyperlink(new AbstractAction("Reset to defaults") {
      @Override
      public void actionPerformed(ActionEvent e) {
        resetColorsToDefault();
      }
    }), panel);

  }

  private void resetColorsToDefault() {
    themeKeys.forEach(tk -> otrosApplication.getTheme().clear(tk));
    listOfColorsInSchme.repaint();
    updateLogDetails();
  }

  private void updateLogDetails() {
    final PluginableElementsContainer<MessageColorizer> messageColorizers = otrosApplication.getAllPluginables().getMessageColorizers();
    final List<MessageColorizer> withModifiedSearch = messageColorizers.getElements()
      .stream()
      .filter(mc -> !(mc instanceof SearchResultColorizer))
      .collect(Collectors.toList());
    final Theme theme = otrosApplication.getTheme();
    withModifiedSearch.add(new SearchResultColorizer(theme, SearchAction.SearchMode.STRING_CONTAINS, "wrong happened"));

    new FormatMessageDialogWorker(
      otrosApplication,
      logData,
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
      logDetailsTextPane,
      new PluginableElementsContainer<>(withModifiedSearch),
      otrosApplication.getAllPluginables().getMessageFormatters(),
      1024).execute();
  }

  private UIManager.LookAndFeelInfo[] getLookAndFeels() {
    final List<UIManager.LookAndFeelInfo> installed = Arrays
      .stream(UIManager.getInstalledLookAndFeels())
      .filter(ui -> !ui.getName().toLowerCase().matches("cde/motif|metal|windows classic|nimbus"))
      .collect(Collectors.toList());

    final List<BasicLookAndFeel> substance = Arrays.asList(
      new SubstanceBusinessLookAndFeel(),
      new SubstanceGraphiteAquaLookAndFeel()
    );

    final List<UIManager.LookAndFeelInfo> extraLf =
      substance.stream()
        .map(l -> new UIManager.LookAndFeelInfo(l.getName(), l.getClass().getName()))
        .collect(Collectors.toList());
    final ArrayList<UIManager.LookAndFeelInfo> result = new ArrayList<>();
    result.addAll(installed);
    result.addAll(extraLf);
    return result.toArray(new UIManager.LookAndFeelInfo[0]);
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
    if (customFontSize.isSelected()) {
      SwingUtilities.invokeLater(() -> new FontSize(otrosApplication, (Integer) model.getValue()).actionPerformed(null));
    }
  }

  private static class MyDefaultListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      final UIManager.LookAndFeelInfo feelInfo = (UIManager.LookAndFeelInfo) value;
      ((JLabel) component).setText(feelInfo.getName());
      try {
        final LookAndFeel lf = (LookAndFeel) Class.forName(feelInfo.getClassName()).newInstance();
        component.setBackground(lf.getDefaults().getColor(isSelected ? "ComboBox.selectionBackground" : "Label.background"));
        component.setForeground(lf.getDefaults().getColor(isSelected ? "ComboBox.selectionForeground" : "Label.foreground"));
        component.setFont(lf.getDefaults().getFont("Label.font"));
      } catch (Throwable e) {
        LOGGER.error("Can't get look and feel properties for " + feelInfo.getName() + "/" + feelInfo.getClassName(), e);
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
