package pl.otros.logview.gui.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTextArea;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.gui.open.AdvanceOpenPanel;
import pl.otros.logview.gui.session.Session;
import pl.otros.logview.gui.session.SessionDeserializer;
import pl.otros.logview.gui.session.SessionSerializer;
import pl.otros.logview.gui.session.SessionUtil;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;
import pl.otros.swing.functional.StringListCellRenderer;
import pl.otros.swing.list.MutableListModel;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;

public class SessionsConfig extends AbstractConfigView implements InMainConfig {

  private final JPanel panel;
  private final PersistService persistService;
  private final MutableListModel<Session> listModel;
  private final AbstractAction deleteAction;
  private final AbstractAction renameAction;
  private final JXTextArea sessionFilesTextArea;
  private final JXList list;

  public SessionsConfig(OtrosApplication otrosApplication) {
    super("sessions", "Sessions", "Manage sessions");
    persistService = otrosApplication.getServices().getPersistService();
    panel = new JPanel(new MigLayout());
    listModel = new MutableListModel<>();
    list = new JXList(listModel);
    list.setCellRenderer(new StringListCellRenderer<>(Session::getName));
    sessionFilesTextArea = new JXTextArea();
    sessionFilesTextArea.setEditable(false);

    sessionFilesTextArea.setColumns(60);
    sessionFilesTextArea.setRows(10);
    sessionFilesTextArea.setPrompt("Select session");

    deleteAction = new AbstractAction("Delete", Icons.DELETE) {
      @Override
      public void actionPerformed(ActionEvent e) {
        final int i = JOptionPane.showConfirmDialog(
          SessionsConfig.this.getView(),
          "Do you want to delete session " + selectedSession().getName(),
          "Confirm",
          JOptionPane.YES_NO_OPTION
        );
        if (i == JOptionPane.YES_OPTION) {
          listModel.remove(list.getSelectedIndex());
        }
      }
    };


    renameAction = new AbstractAction("Rename", Icons.EDIT_SIGNATURE) {

      @Override
      public void actionPerformed(ActionEvent e) {
        final Session selectedSession = selectedSession();
        final String initialSelectionValue = selectedSession.getName();
        final String newName = (String) showInputDialog(panel, "Rename session", "Enter new name for session", PLAIN_MESSAGE, null, null, initialSelectionValue);
        if (newName != null && !newName.equals(initialSelectionValue)) {
          final List<Session> sessions = listModel.getList();
          final boolean present = sessions.stream().map(Session::getName).anyMatch(s -> s.equals(newName));
          if (present) {
            final int confirm = JOptionPane.showConfirmDialog(panel, "Do you want to override session \"" + newName + "\"", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.NO_OPTION) {
              return;
            }
          }
          listModel.change(list.getSelectedIndex(), new Session(newName, selectedSession.getFilesToOpen()));
        }
      }
    };


    list.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "Delete");
    list.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
    list.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "Edit");
    list.getActionMap().put("Delete", deleteAction);
    list.getActionMap().put("Edit", renameAction);

    listModel.addListDataListener(new ListDataListener() {
      @Override
      public void intervalAdded(ListDataEvent e) {
        updateState();
      }

      @Override
      public void intervalRemoved(ListDataEvent e) {
        updateState();
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
        updateState();
      }
    });
    list.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        updateState();
      }
    });
    panel.add(new JLabel("List of saved sessions:"));
    panel.add(new JLabel("Session detail"), "wrap");
    panel.add(new JScrollPane(list), "pushx, aligny top, growx");
    panel.add(new JScrollPane(sessionFilesTextArea), "push, aligny top, growy, wrap");
    panel.add(new JButton(renameAction));
    panel.add(new JButton(deleteAction));
  }

  private Session selectedSession() {
    return listModel.getElementAt(list.getSelectedIndex());
  }

  private void updateState() {
    boolean empty = listModel.getSize() == 0;
    boolean selected = list.getSelectedIndex() > -1;
    renameAction.setEnabled(!empty && selected);
    deleteAction.setEnabled(!empty && selected);
    if (empty) {
      sessionFilesTextArea.setText("");
    }
    if (selected && !empty && list.getSelectedIndex() < listModel.getSize()) {
      final Session session = selectedSession();
      sessionFilesTextArea.setText(SessionUtil.toStringGroupedByServer(session));
    } else {
      sessionFilesTextArea.setText("");
    }

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
    final List<Session> loaded = persistService.load(AdvanceOpenPanel.SESSIONS, new ArrayList<>(), new SessionDeserializer());
    listModel.clear();
    listModel.addAll(loaded);
  }


  @Override
  public void saveConfiguration(Configuration c) {
    try {
      persistService.persist(AdvanceOpenPanel.SESSIONS, listModel.getList(), new SessionSerializer());
    } catch (Exception e) {
      JOptionPane.showMessageDialog(panel, "Can't save: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}
