package pl.otros.logview.gui.config;

import com.google.common.base.Joiner;
import jsyntaxpane.DefaultSyntaxKit;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXRadioGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.io.Utils;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;
import pl.otros.swing.list.MutableListModel;
import pl.otros.swing.table.ColumnLayout;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.SelectionMode;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static javax.swing.JOptionPane.*;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.VERTICAL;
import static pl.otros.logview.gui.ConfKeys.*;

public class LogTableFormatConfigView extends AbstractConfigView implements InMainConfig {

  public static final String DEFAULT_ABBREVIATION_HEADER =
      "#You can define abbreviations for the packages you use\n" +
          "#put here package abbreviations like in Eclipse (http://java.dzone.com/articles/eclipse-tip-help-tidy-package)\n" +
          "my.package.project={MP}\n\n";
  public static final String ACTION_RENAME = "rename";
  public static final String ACTION_DELETE = "delete";
  public static final String COL_LAYOUT = "colLayout";
  public static final String ACTION_COPY_SELECTED = "copy";
  public static final String ACTION_PASTE = "paste";
  public static final String VIEW_ID = "logDisplay";
  private static final Logger LOGGER = LoggerFactory.getLogger(LogTableFormatConfigView.class.getName());
  private final String[] dateFormats;
  private final JXRadioGroup radioGroup;
  private final JXComboBox dateFormatRadio;
  private final JPanel panel;
  private final JEditorPane packageAbbreviationTa;
  private final JList columnLayoutsList;
  private MutableListModel<ColumnLayout> columnLayoutListModel;
  private final OtrosApplication otrosApplication;
  private JOtrosVfsBrowserDialog jOtrosVfsBrowserDialog;

  public LogTableFormatConfigView(final OtrosApplication otrosApplication) {
    super(VIEW_ID, "Log event display", "This configuration provides allow user to change how log events are displayed");
    this.otrosApplication = otrosApplication;
    panel = new JPanel();
    panel.setLayout(new MigLayout());
    dateFormats = new String[]{
        "HH:mm:ss", //
        "HH:mm:ss.SSS",//
        "dd-MM HH:mm:ss.SSS",//
        "E HH:mm:ss", //
        "E HH:mm:ss.SS", //
        "MMM dd. HH:mm:ss",//
    };
    dateFormatRadio = new JXComboBox(dateFormats);
    addLabel("Date format", 'd', dateFormatRadio, panel);
    final JTextField exampleTextField = new JTextField(20);
    exampleTextField.setEditable(false);
    addLabel("Format example", 'e', exampleTextField, panel);
    dateFormatRadio.addActionListener(e -> exampleTextField.setText(new SimpleDateFormat(dateFormatRadio.getSelectedItem().toString()).format(new Date())));
    dateFormatRadio.setSelectedIndex(0);
    radioGroup = new JXRadioGroup(LevelRenderer.Mode.values());
    addLabel("Level display", 'l', radioGroup, panel);

    DefaultSyntaxKit.initKit();
    packageAbbreviationTa = new JEditorPane();
    final JScrollPane packageAbbreviationSp = new JScrollPane(packageAbbreviationTa);
    packageAbbreviationTa.setText(DEFAULT_ABBREVIATION_HEADER);
    packageAbbreviationTa.setContentType("text/properties");
    packageAbbreviationTa.setPreferredSize(new Dimension(500, 200));
    packageAbbreviationTa.setToolTipText(DEFAULT_ABBREVIATION_HEADER);

    addLabel("Package abbreviation", 'a', packageAbbreviationSp, panel);


    //Column layouts
    final JPanel columnLayoutsPanel = new JPanel(new BorderLayout());
    columnLayoutListModel = new MutableListModel<>();

    columnLayoutsList = new JList(columnLayoutListModel);
    columnLayoutsList.setToolTipText("Click right mouse button to edit or delete");
    columnLayoutsList.setMinimumSize(new Dimension(100, 40));
    columnLayoutsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    columnLayoutsList.setCellRenderer(new ColumnLayoutRenderer());
    final ActionMap actionMap = columnLayoutsList.getActionMap();
    final AbstractAction deleteAction = new AbstractAction("Delete", Icons.DELETE) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final ColumnLayout layout = columnLayoutListModel.getElementAt(columnLayoutsList.getSelectedIndex());
        final int i = showConfirmDialog(panel, "Do you want to delete column layout " + layout.getName(), "Remove", YES_NO_OPTION);
        if (OK_OPTION == i) {
          columnLayoutListModel.remove(columnLayoutsList.getSelectedIndex());
        }

      }
    };
    final AbstractAction renameAction = new AbstractAction("Rename", Icons.EDIT_SIGNATURE) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final int selectedIndex = columnLayoutsList.getSelectedIndex();
        final ColumnLayout o = columnLayoutListModel.getElementAt(selectedIndex);
        final String newName = (String) showInputDialog(panel, "Rename column layout", "Enter new name for column layout", PLAIN_MESSAGE, null, null, o.getName());
        if (newName != null) {
          columnLayoutListModel.change(selectedIndex, new ColumnLayout(newName, o.getColumns()));
        }
      }
    };
    columnLayoutsList.addListSelectionListener(listSelectionEvent -> {
      boolean selected = columnLayoutsList.getSelectedIndices().length > 0;
      renameAction.setEnabled(selected);
      deleteAction.setEnabled(selected);
    });
    renameAction.setEnabled(false);
    deleteAction.setEnabled(false);
    columnLayoutsList.setSelectedIndices(new int[0]);
    columnLayoutsList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


    final JToolBar importToolbar = new JToolBar(VERTICAL);
    importToolbar.add(new JLabel("Export: "));

    final AbstractAction exportSelectedToClipboard = new CopyAllOrSelectedToClipboardAction(columnLayoutsPanel);
    importToolbar.add(exportSelectedToClipboard);

    importToolbar.add(new ExportToFileAction(columnLayoutsPanel));

    importToolbar.add(new JLabel("Import: "));
    final AbstractAction importFromClipboard = new PasteFomClipboardAction(columnLayoutsPanel);
    importToolbar.add(importFromClipboard);
    final AbstractAction importFromFileAction = new ImportFromFileAction(otrosApplication, columnLayoutsPanel);
    importToolbar.add(importFromFileAction);


    actionMap.put(ACTION_DELETE, deleteAction);
    actionMap.put(ACTION_RENAME, renameAction);
    actionMap.put(ACTION_COPY_SELECTED, exportSelectedToClipboard);
    actionMap.put(ACTION_PASTE, importFromClipboard);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_RENAME);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_DELETE);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), ACTION_DELETE);

    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), ACTION_PASTE);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK), ACTION_PASTE);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), ACTION_COPY_SELECTED);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK), ACTION_COPY_SELECTED);

    final JScrollPane scrollPane = new JScrollPane(columnLayoutsList);
    scrollPane.setMinimumSize(new Dimension(100, 40));
    columnLayoutsList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          columnLayoutsList.getActionMap().get(ACTION_RENAME).actionPerformed(null);
        }
      }
    });
    addPopupMenuWithActionFromActionMap(columnLayoutsList, ACTION_COPY_SELECTED, ACTION_PASTE, ACTION_RENAME, ACTION_DELETE);

    final String hintText = "Column layouts can be exported/imported from clipboard or file";
    final JLabel hintLabel = new JLabel(hintText, Icons.LEVEL_INFO, LEFT);

    columnLayoutsPanel.add(scrollPane);
    columnLayoutsPanel.add(importToolbar, BorderLayout.EAST);
    columnLayoutsPanel.add(hintLabel, BorderLayout.SOUTH);

    addLabel("Column layouts", 'c', columnLayoutsPanel, panel);
  }

  //TODO extract save/load method common place
  public static void saveColumnLayouts(List<ColumnLayout> list, Configuration c) {
    final DataConfiguration dc = new DataConfiguration(c);
    final Iterator<String> keys = dc.getKeys(COL_LAYOUT);
    while (keys.hasNext()) {
      dc.clearProperty(keys.next());
    }

    dc.setProperty(COL_LAYOUT + ".count", list.size());
    for (int i = 0; i < list.size(); i++) {
      dc.setProperty(String.format("%s._%d.name", COL_LAYOUT, i), list.get(i).getName());
      dc.setProperty(String.format("%s._%d.columns", COL_LAYOUT, i), list.get(i).getColumns());
    }

  }

  public static List<ColumnLayout> loadColumnLayouts(Configuration configuration) {
    List<ColumnLayout> layouts = new ArrayList<>();
    DataConfiguration dc = new DataConfiguration(configuration);
    final int count = dc.getInt(COL_LAYOUT + ".count", 0);
    for (int i = 0; i < count; i++) {
      String columnLayoutName = dc.getString(COL_LAYOUT + "._" + i + ".name");
      List<String> list = dc.getList(String.class, COL_LAYOUT + "._" + i + ".columns");
      ColumnLayout columnLayout = new ColumnLayout(columnLayoutName, list);
      layouts.add(columnLayout);
    }
    return layouts;
  }

  public JOtrosVfsBrowserDialog getjOtrosVfsBrowserDialog(OtrosApplication otrosApplication) {
    if (jOtrosVfsBrowserDialog == null) {
      jOtrosVfsBrowserDialog = new JOtrosVfsBrowserDialog(otrosApplication.getConfiguration());
    }
    return jOtrosVfsBrowserDialog;
  }

  private void importFromFile(FileObject file) throws ConfigurationException, FileSystemException {
    try {
      final XMLConfiguration xmlConfiguration = new XMLConfiguration();
      xmlConfiguration.load(file.getContent().getInputStream());
      final List<ColumnLayout> columnLayouts = loadColumnLayouts(xmlConfiguration);
      importColumnLayouts(columnLayouts);
      otrosApplication.getStatusObserver().updateStatus(String.format("Column layouts have been imported from %s", file.getName().getFriendlyURI()));
    } finally {
      Utils.closeQuietly(file);
    }
  }

  private void importFromClipboard() throws IOException, UnsupportedFlavorException, ConfigurationException {
    XMLConfiguration xmlConfiguration;
    try {
      String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
      StringReader stringReader = new StringReader(data);
      xmlConfiguration = new XMLConfiguration();
      xmlConfiguration.load(stringReader);
      final List<ColumnLayout> columnLayouts = loadColumnLayouts(xmlConfiguration);
      importColumnLayouts(columnLayouts);
      otrosApplication.getStatusObserver().updateStatus("Column layouts have been imported");
    } catch (Exception e) {
      LOGGER.error( "Can't import table layout from clipboard", e);
      JOptionPane.showMessageDialog(panel.getRootPane(), "Can't import from clipboard");
    }
  }

  private void importColumnLayouts(List<ColumnLayout> columnLayouts) {
    if (columnLayouts.isEmpty()) {
      JOptionPane.showMessageDialog(panel.getRootPane(), "No column layout in clipboard have been found");
      return;
    }
    JPanel messagePanel = new JPanel(new BorderLayout());
    final MutableListModel<ColumnLayout> listModel = new MutableListModel<>();
    columnLayouts.forEach(listModel::add);

    final JList jList = new JList(listModel);
    jList.setCellRenderer(new ColumnLayoutRenderer());
    jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    if (listModel.getSize() > 0) {
      jList.getSelectionModel().setSelectionInterval(0, listModel.getSize() - 1);
    }
    messagePanel.add(new JScrollPane(jList));
    final int resp = JOptionPane.showConfirmDialog(LogTableFormatConfigView.this.panel.getRootPane(), messagePanel, "Select column layouts to import",
        JOptionPane.OK_CANCEL_OPTION);
    if (resp == JOptionPane.CANCEL_OPTION) {
      return;
    }

    final int[] selectedIndices = jList.getSelectedIndices();
    for (int selectedIndex : selectedIndices) {
      final ColumnLayout elementAt = listModel.getElementAt(selectedIndex);
      columnLayoutListModel.add(elementAt);
    }

  }

  private void exportToFile(File file, List<ColumnLayout> columnLayouts) throws ConfigurationException, FileNotFoundException {
    FileOutputStream out = null;
    try {
      final XMLConfiguration xmlConfiguration = new XMLConfiguration();
      saveColumnLayouts(columnLayouts, xmlConfiguration);
      out = new FileOutputStream(file);
      xmlConfiguration.save(out);
      otrosApplication.getStatusObserver().updateStatus(String.format("Column layouts have been exported to %s", file.getAbsolutePath()));
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  private void exportToClipBoard(List<ColumnLayout> columnLayouts) throws ConfigurationException {
    final XMLConfiguration xmlConfiguration = new XMLConfiguration();
    saveColumnLayouts(columnLayouts, xmlConfiguration);
    final StringWriter writer = new StringWriter();
    xmlConfiguration.save(writer);
    StringSelection stringSelection = new StringSelection(writer.getBuffer().toString());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, null);
    otrosApplication.getStatusObserver().updateStatus("Column layouts have been exported to clipboard");
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
  public void loadConfiguration(Configuration configuration) {
    LevelRenderer.Mode mode = LevelRenderer.Mode.valueOf(configuration.getString(LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly.name()));
    radioGroup.setSelectedValue(mode);
    String dateFormat = StringUtils.defaultIfBlank(configuration.getString(LOG_TABLE_FORMAT_DATE_FORMAT), dateFormats[1]);
    dateFormatRadio.setSelectedItem(dateFormat);

    packageAbbreviationTa.setText(configuration.getString(ConfKeys.LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS, DEFAULT_ABBREVIATION_HEADER));
    packageAbbreviationTa.setCaretPosition(packageAbbreviationTa.getText().length());

    columnLayoutListModel = new MutableListModel<>();
    columnLayoutsList.setModel(columnLayoutListModel);
    loadColumnLayouts(configuration).forEach(columnLayoutListModel::add);
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(LOG_TABLE_FORMAT_LEVEL_RENDERER, ((LevelRenderer.Mode) radioGroup.getSelectedValue()).name());
    c.setProperty(LOG_TABLE_FORMAT_DATE_FORMAT, dateFormatRadio.getSelectedItem());
    c.setProperty(LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS, packageAbbreviationTa.getText());

    saveColumnLayouts(columnLayoutListModel.getList(), c);
  }

  //TODO move to some common
  private JPopupMenu addPopupMenuWithActionFromActionMap(JComponent list, String... actions) {
    JPopupMenu favoritesPopupMenu = new JPopupMenu();
    for (String action : actions) {
      favoritesPopupMenu.add(list.getActionMap().get(action));
    }
    list.addKeyListener(new pl.otros.logview.gui.PopupListener(favoritesPopupMenu));
    list.addMouseListener(new pl.otros.logview.gui.PopupListener(favoritesPopupMenu));
    return favoritesPopupMenu;
  }

  private class CopyAllOrSelectedToClipboardAction extends AbstractAction {
    private final JPanel columnLayoutsPanel;

    public CopyAllOrSelectedToClipboardAction(JPanel columnLayoutsPanel) {
      super("Copy selected to clipboard", Icons.DOCUMENT_COPY);
      putValue(SHORT_DESCRIPTION, this.getValue(NAME));
      this.columnLayoutsPanel = columnLayoutsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      try {
        List<ColumnLayout> list = new ArrayList<>();
        final Object[] selectedValues = LogTableFormatConfigView.this.columnLayoutsList.getSelectedValues();
        if (selectedValues.length == 0) {
          list.addAll(columnLayoutListModel.getList());
        } else {
          for (Object selectedValue : selectedValues) {
            list.add((ColumnLayout) selectedValue);
          }
        }
        exportToClipBoard(list);
      } catch (ConfigurationException e) {
        LOGGER.error( "Can't export column layouts. ", e);
        JOptionPane.showMessageDialog(columnLayoutsPanel.getRootPane(), "Can't export column layout to clipboard: " + e.getMessage(),
            "Export error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private class ExportToFileAction extends AbstractAction {
    private final JPanel columnLayoutsPanel;

    public ExportToFileAction(JPanel columnLayoutsPanel) {
      super("Export to file", Icons.TABLE_EXPORT);
      putValue(SHORT_DESCRIPTION, this.getValue(NAME));
      this.columnLayoutsPanel = columnLayoutsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle("Select file");
      if (chooser.showSaveDialog(panel.getRootPane()) == JFileChooser.APPROVE_OPTION) {
        final File selectedFile = chooser.getSelectedFile();
        try {
          exportToFile(selectedFile, columnLayoutListModel.getList());
        } catch (Exception e) {
          LOGGER.error( "Can't export column layouts to file " + selectedFile, e);
          JOptionPane.showMessageDialog(columnLayoutsPanel.getRootPane(), "Can't export column layout to file: " + e.getMessage(), "Export error", JOptionPane
              .ERROR_MESSAGE);
        }
      }
    }
  }

  private class PasteFomClipboardAction extends AbstractAction {
    private final JPanel columnLayoutsPanel;

    public PasteFomClipboardAction(JPanel columnLayoutsPanel) {
      super("Paste from clipboard", Icons.CLIPBOARD_PASTE);
      putValue(SHORT_DESCRIPTION, this.getValue(NAME));
      this.columnLayoutsPanel = columnLayoutsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      try {
        importFromClipboard();
      } catch (Exception e) {
        LOGGER.error( "Can't import column layout from clipboard", e);
        JOptionPane.showMessageDialog(columnLayoutsPanel.getRootPane(), "Can't import column layout from clipboard: " + e.getMessage(),
            "Paste error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private class ImportFromFileAction extends AbstractAction {
    private final OtrosApplication otrosApplication;
    private final JPanel columnLayoutsPanel;

    public ImportFromFileAction(OtrosApplication otrosApplication, JPanel columnLayoutsPanel) {
      super("Import from file", Icons.TABLE_IMPORT);
      putValue(SHORT_DESCRIPTION, this.getValue(NAME));
      this.otrosApplication = otrosApplication;
      this.columnLayoutsPanel = columnLayoutsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      final JOtrosVfsBrowserDialog dialog = getjOtrosVfsBrowserDialog(otrosApplication);
      dialog.setMultiSelectionEnabled(false);
      dialog.setSelectionMode(SelectionMode.FILES_ONLY);

      final JOtrosVfsBrowserDialog.ReturnValue returnValue = dialog.showOpenDialog(panel.getRootPane(), "Select file with column layout to import");
      if (returnValue == JOtrosVfsBrowserDialog.ReturnValue.Approve) {
        final FileObject selectedFile = dialog.getSelectedFile();
        try {
          importFromFile(selectedFile);
        } catch (ConfigurationException e) {
          LOGGER.error( "Can't import column layout from file", e);
          JOptionPane.showMessageDialog(columnLayoutsPanel.getRootPane(), "Can't import column layout from clipboard: " + e.getMessage(), "Import error",
              JOptionPane.ERROR_MESSAGE);
        } catch (FileSystemException e) {
          LOGGER.error( "Can't import column layout from file", e);
          JOptionPane.showMessageDialog(columnLayoutsPanel.getRootPane(), "Can't import column layout from clipboard: " + e.getMessage(), "Import error",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }
}


class ColumnLayoutRenderer extends DefaultListCellRenderer {
  @Override
  public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
    JLabel label = (JLabel) super.getListCellRendererComponent(jList, o, i, b, b1);
    if (o instanceof ColumnLayout) {
      ColumnLayout columnLayout = (ColumnLayout) o;
      label.setText(" " + columnLayout.getName() + " [" + Joiner.on(", ").join(columnLayout.getColumns()) + "]");
    }
    return label;
  }


}
