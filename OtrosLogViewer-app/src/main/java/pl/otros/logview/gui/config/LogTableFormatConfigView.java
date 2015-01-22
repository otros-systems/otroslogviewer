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
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.gui.table.ColumnLayout;
import pl.otros.logview.io.Utils;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.SelectionMode;
import pl.otros.vfs.browser.VfsBrowser;
import pl.otros.vfs.browser.list.MutableListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.JOptionPane.*;
import static pl.otros.logview.gui.ConfKeys.*;

public class LogTableFormatConfigView extends AbstractConfigView implements InMainConfig {

  private static final Logger LOGGER = Logger.getLogger(LogTableFormatConfigView.class.getName());

  public static final String DEFAULT_ABBREVIATION_HEADER =
      "#You can define abbreviations for the packages you use\n" +
          "#put here package abbreviations like in Eclipse (http://java.dzone.com/articles/eclipse-tip-help-tidy-package)\n" +
          "my.package.project={MP}\n\n";
  public static final String ACTION_RENAME = "rename";
  public static final String ACTION_DELETE = "delete";
  public static final String COL_LAYOUT = "colLayout";

  private final String[] dateFormats;
  private final JXRadioGroup radioGroup;
  private final JXComboBox dateFormatRadio;
  private final JPanel panel;
  private final JEditorPane packageAbbreviationTa;
  private final JList columnLayoutsList;
  private MutableListModel<ColumnLayout> columnLayoutListModel;
  private OtrosApplication otrosApplication;
  private VfsBrowser vfsBrowser;
  private JOtrosVfsBrowserDialog jOtrosVfsBrowserDialog;

  public LogTableFormatConfigView(final OtrosApplication otrosApplication) {
    super("logDisplay", "Log event display", "This configuration provides allow user to change how log events are displayed");
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
    dateFormatRadio.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        exampleTextField.setText(new SimpleDateFormat(dateFormatRadio.getSelectedItem().toString()).format(new Date()));
      }
    });
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

    columnLayoutListModel = new MutableListModel<ColumnLayout>();

    columnLayoutsList = new JList(columnLayoutListModel);
    columnLayoutsList.setMinimumSize(new Dimension(100, 40));
    columnLayoutsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    columnLayoutsList.setCellRenderer(new ColumnLayoutRenderer());
    final ActionMap actionMap = columnLayoutsList.getActionMap();
    final AbstractAction deleteAction = new AbstractAction("Delete") {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final int i = showConfirmDialog(panel, "Do you want to delete column layout //TODO name", "Rename", YES_NO_OPTION);
        if (OK_OPTION == i) {
          columnLayoutListModel.remove(columnLayoutsList.getSelectedIndex());
        }

      }
    };
    final AbstractAction renameAction = new AbstractAction("Rename") {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final int selectedIndex = columnLayoutsList.getSelectedIndex();
        final ColumnLayout o = columnLayoutListModel.getElementAt(selectedIndex);
        final String newName = (String) showInputDialog(panel, "Rename column layout", "Enter new name for column layout", PLAIN_MESSAGE, null, null, o.getName());
        if (newName != null) {
          //TODO check for duplicate
          columnLayoutListModel.change(selectedIndex, new ColumnLayout(newName, o.getColumns()));
        }
      }
    };
    columnLayoutsList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        boolean selected = columnLayoutsList.getSelectedIndices().length>0;
        renameAction.setEnabled(selected);
        deleteAction.setEnabled(selected);
      }
    });
    renameAction.setEnabled(false);
    deleteAction.setEnabled(false);
    columnLayoutsList.setSelectedIndices(new int[0]);

    actionMap.put(ACTION_DELETE, deleteAction);
    actionMap.put(ACTION_RENAME, renameAction);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_RENAME);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_DELETE);
    columnLayoutsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), ACTION_DELETE);

    final JScrollPane scrollPane = new JScrollPane(columnLayoutsList);
    scrollPane.setMinimumSize(new Dimension(100, 40));
    columnLayoutsList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          columnLayoutsList.getActionMap().get(ACTION_RENAME).actionPerformed(null);
        }
      }
    });
    addPopupMenuWithActionFromActionMap(columnLayoutsList, ACTION_RENAME, ACTION_DELETE);

    final JToolBar exportToolbar = new JToolBar();
    exportToolbar.add(new AbstractAction("Export all to clipboard",Icons.CLIPBOARD_SIGN_OUT) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        try {
          exportToClipBoard(columnLayoutListModel.getList());
        } catch (ConfigurationException e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(exportToolbar.getRootPane(), "Can't export column layout to clipboard: " + e.getMessage(), "Export error", JOptionPane
              .ERROR_MESSAGE);
        }
      }
    });
    exportToolbar.add(new AbstractAction("Export selected to clipboard",Icons.CLIPBOARD_SIGN_OUT) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        try {
          final Object[] selectedValues = LogTableFormatConfigView.this.columnLayoutsList.getSelectedValues();
          if (selectedValues.length == 0) {
            return;
          }
          List<ColumnLayout> list = new ArrayList<ColumnLayout>();
          for (Object selectedValue : selectedValues) {
            list.add((ColumnLayout) selectedValue);
          }
          exportToClipBoard(list);
        } catch (ConfigurationException e) {
          LOGGER.log(Level.SEVERE, "Can't export column layouts. ", e);
          JOptionPane.showMessageDialog(exportToolbar.getRootPane(), "Can't export column layout to clipboard: " + e.getMessage(), "Export error", JOptionPane
              .ERROR_MESSAGE);
        }
      }
    });
    exportToolbar.add(new AbstractAction("Export to file", Icons.TABLE_EXPORT) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select file");
        if (chooser.showSaveDialog(panel.getRootPane()) == JFileChooser.APPROVE_OPTION) {
          final File selectedFile = chooser.getSelectedFile();
          try {
            exportToFile(selectedFile, columnLayoutListModel.getList());
          } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Can't export column layouts to file " + selectedFile, e);
            JOptionPane.showMessageDialog(exportToolbar.getRootPane(), "Can't export column layout to file: " + e.getMessage(), "Export error", JOptionPane
                .ERROR_MESSAGE);
          }
        }
      }
    });

    final JToolBar importToolbar = new JToolBar();
    importToolbar.add(new AbstractAction("Import from clipboard",Icons.CLIPBOARD_SIGN) {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        try {
          importFromClipboard();

        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Can't import column layout from clipboard", e);
          JOptionPane.showMessageDialog(importToolbar.getRootPane(), "Can't import column layout from clipboard: " + e.getMessage(), "Import error", JOptionPane
              .ERROR_MESSAGE);
        }
      }
    });
    importToolbar.add(new AbstractAction("Import from file", Icons.TABLE_IMPORT) {
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
            LOGGER.log(Level.SEVERE, "Can't import column layout from file", e);
            JOptionPane.showMessageDialog(importToolbar.getRootPane(), "Can't import column layout from clipboard: " + e.getMessage(), "Import error",
                JOptionPane.ERROR_MESSAGE);
          } catch (FileSystemException e) {
            LOGGER.log(Level.SEVERE, "Can't import column layout from file", e);
            JOptionPane.showMessageDialog(importToolbar.getRootPane(), "Can't import column layout from clipboard: " + e.getMessage(), "Import error",
                JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    });


    addLabel("Column layouts", 'c', scrollPane, panel);
    addLabel("Export", 'x', exportToolbar, panel);
    addLabel("Import", 'i', importToolbar, panel);
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
    XMLConfiguration xmlConfiguration = null;
    try {
      String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
      StringReader stringReader = new StringReader(data);
      xmlConfiguration = new XMLConfiguration();
      xmlConfiguration.load(stringReader);
      final List<ColumnLayout> columnLayouts = loadColumnLayouts(xmlConfiguration);
      importColumnLayouts(columnLayouts);
      otrosApplication.getStatusObserver().updateStatus("Column layouts have been imported");
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't import table layout from clipboard", e);
      JOptionPane.showMessageDialog(panel.getRootPane(), "Can't import from clipboard");
    }
  }

  private void importColumnLayouts(List<ColumnLayout> columnLayouts) {
    if (columnLayouts.isEmpty()) {
      JOptionPane.showMessageDialog(panel.getRootPane(), "No column layout in clipboard have been found");
      return;
    }
    JPanel messagePanel = new JPanel(new BorderLayout());
    final MutableListModel<ColumnLayout> listModel = new MutableListModel<ColumnLayout>();
    for (ColumnLayout columnLayout : columnLayouts) {
      listModel.add(columnLayout);
    }

    final JList jList = new JList(listModel);
    jList.setCellRenderer(new ColumnLayoutRenderer());
    jList.getSelectionModel().setSelectionInterval(0, listModel.getSize());
    messagePanel.add(new JScrollPane(jList));
    final int resp = JOptionPane.showConfirmDialog(LogTableFormatConfigView.this.panel.getRootPane(), messagePanel, "Select column layouts to import",
        JOptionPane.OK_CANCEL_OPTION);
    if (resp == JOptionPane.CANCEL_OPTION) {
      return;
    }
    jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    final int[] selectedIndices = jList.getSelectedIndices();
    for (int i = 0; i < selectedIndices.length; i++) {
      columnLayouts.add(columnLayoutListModel.getElementAt(i));
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

    columnLayoutListModel = new MutableListModel<ColumnLayout>();
    columnLayoutsList.setModel(columnLayoutListModel);
    for (ColumnLayout layout : loadColumnLayouts(configuration)) {
      columnLayoutListModel.add(layout);
    }
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(LOG_TABLE_FORMAT_LEVEL_RENDERER, ((LevelRenderer.Mode) radioGroup.getSelectedValue()).name());
    c.setProperty(LOG_TABLE_FORMAT_DATE_FORMAT, dateFormatRadio.getSelectedItem());
    c.setProperty(LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS, packageAbbreviationTa.getText());

    saveColumnLayouts(columnLayoutListModel.getList(), c);
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
    List<ColumnLayout> layouts = new ArrayList<ColumnLayout>();
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