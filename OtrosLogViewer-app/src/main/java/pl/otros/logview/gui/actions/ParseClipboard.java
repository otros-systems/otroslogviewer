package pl.otros.logview.gui.actions;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.Utils;
import pl.otros.vfs.browser.util.VFSUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ParseClipboard extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParseClipboard.class);

  public ParseClipboard(OtrosApplication otrosApplication) {
    super("Parse clipboard", otrosApplication);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    final String data;
    try {
      data = (String) systemClipboard.getData(DataFlavor.stringFlavor);
    } catch (UnsupportedFlavorException | IOException e1) {
      JOptionPane.showMessageDialog(getOtrosApplication().getApplicationJFrame(), "Can't get clipboard content: " + e1.getMessage());
      return;
    }
    String shortData = data.substring(0, Math.min(1024 * 100, data.length()));

    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    progressBar.setStringPainted(true);
    progressBar.setString("Detecting logs");
    final JDialog dialog = new JDialog(getOtrosApplication().getApplicationJFrame());
    dialog.setModal(true);
    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(progressBar);
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(x -> dialog.dispose());
    dialog.getContentPane().add(cancelButton, BorderLayout.SOUTH);
    dialog.setUndecorated(true);
    dialog.pack();
    dialog.setLocationRelativeTo(getOtrosApplication().getApplicationJFrame());


    final Callable<PossibleLogImporters> possibleLogImportersCallable = possibleLogImportersCallable(shortData);
    final SwingWorker<PossibleLogImporters, Void> swingWorker = new SwingWorker<PossibleLogImporters, Void>() {

      @Override
      protected PossibleLogImporters doInBackground() throws Exception {
        return possibleLogImportersCallable.call();
      }

      @Override
      protected void done() {
        try {
          final PossibleLogImporters result = get();
          dialog.dispose();
          final Optional<LogImporter> logImporter = result.getLogImporter();
          if (logImporter.isPresent()) {
            JPanel panel = new JPanel(new MigLayout());
            panel.add(new JLabel("Parse log from clipboard using log parser: "));
            final JComboBox<LogImporter> comp = new JComboBox<>(result.getAvailableImporters().toArray(new LogImporter[0]));
            logImporter.ifPresent(comp::setSelectedItem);
            panel.add(comp, "wrap");

            panel.add(new JScrollPane(new JTextArea(shortData)), "width 200:500:700, height 200:200:200, span, wrap");

            final int confirm = JOptionPane.showConfirmDialog((Component) e.getSource(), panel, "msg", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.NO_OPTION) {
              return;
            }
            final String dateString = new SimpleDateFormat("HH:mm:ss").format(new Date());

            new TailLogActionListener(getOtrosApplication(), logImporter.get())
              .openFileObjectInTailMode(createFileObjectWithContent(data), "Clipboard " + dateString);

          } else {
            JPanel panel = new JPanel(new MigLayout());
            panel.add(new JLabel("Can't parse clipboard with defined log parsers"), "wrap");
            panel.add(new JScrollPane(new JTextArea(shortData)), "width 200:500:700, height 30:200:200, span, wrap");
            JOptionPane.showMessageDialog(getOtrosApplication().getApplicationJFrame(), panel, "Can't parse log", JOptionPane.WARNING_MESSAGE);
          }
        } catch (Exception e1) {
          LOGGER.error("Can't parse logs from clipboard",e1);
        }
      }
    };

    swingWorker.execute();
    dialog.setVisible(true);
  }

  private FileObject createFileObjectWithContent(String data) throws IOException {
    final File tempFile = File.createTempFile("olv_temp", "");
    OutputStream out = new FileOutputStream(tempFile);
    IOUtils.write(data, out, Charset.forName("UTF-8"));
    IOUtils.closeQuietly(out);
    final FileObject fileObject = VFSUtils.resolveFileObject(tempFile.toURI());
    return fileObject;
  }

  private Callable<PossibleLogImporters> possibleLogImportersCallable(final String data) {
    return () -> {
      final File file = File.createTempFile("olv_clipboard", "");
      OutputStream out = new FileOutputStream(file);
      IOUtils.write(data, out, Charset.forName("UTF-8"));
      IOUtils.closeQuietly(out);
      file.delete();
      final Collection<LogImporter> logImporters = getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements();
      final PossibleLogImporters possibleLogImporters = Utils.detectPossibleLogImporter(logImporters, data.getBytes());
      return possibleLogImporters;
    };
  }
}
