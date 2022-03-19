/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.vfs.browser.demo;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.vfs.browser.SelectionMode;
import pl.otros.vfs.browser.VfsBrowser;
import pl.otros.vfs.browser.i18n.Messages;
import pl.otros.vfs.browser.table.FileSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class TestBrowser {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestBrowser.class);

  public static void main(final String[] args) throws InterruptedException, InvocationTargetException, SecurityException, IOException {
    if (args.length > 1)
      throw new IllegalArgumentException("SYNTAX:  java... "
          + TestBrowser.class.getName() + " [initialPath]");

    SwingUtilities.invokeAndWait(new Runnable() {

      @Override
      public void run() {
        tryLoadSubstanceLookAndFeel();
        final JFrame f = new JFrame("OtrosVfsBrowser demo");
        Container contentPane = f.getContentPane();
        contentPane.setLayout(new BorderLayout());
        DataConfiguration dc = null;
        final PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        File favoritesFile = new File("favorites.properties");
        propertiesConfiguration.setFile(favoritesFile);
        if (favoritesFile.exists()) {
          try {
            propertiesConfiguration.load();
          } catch (ConfigurationException e) {
            e.printStackTrace();
          }
        }
        dc = new DataConfiguration(propertiesConfiguration);
        propertiesConfiguration.setAutoSave(true);
        final VfsBrowser comp =
            new VfsBrowser(dc, (args.length > 0) ? args[0] : null, new ExampleListener());
        comp.setSelectionMode(SelectionMode.FILES_ONLY);
        comp.setMultiSelectionEnabled(true);
        comp.setApproveAction(new AbstractAction(Messages.getMessage("demo.showContentButton")) {
          @Override
          public void actionPerformed(ActionEvent e) {
            FileObject[] selectedFiles = comp.getSelectedFiles();
            System.out.println("Selected files count=" + selectedFiles.length);
            for (FileObject selectedFile : selectedFiles) {
              try {
                FileSize fileSize = new FileSize(selectedFile.getContent().getSize());
                System.out.println(selectedFile.getName().getURI() + ": " + fileSize.toString());
                byte[] bytes = readBytes(selectedFile.getContent().getInputStream(), 150 * 1024);
                JScrollPane sp = new JScrollPane(new JTextArea(new String(bytes)));
                JDialog d = new JDialog(f);
                d.setTitle("Content of file: " + selectedFile.getName().getFriendlyURI());
                d.getContentPane().add(sp);
                d.setSize(600, 400);
                d.setVisible(true);
              } catch (Exception e1) {
                LOGGER.error("Failed to read file", e1);
                JOptionPane.showMessageDialog(f, (e1.getMessage() == null )
                        ? e1.toString() : e1.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
              }
            }
          }
        });

        comp.setCancelAction(new AbstractAction(Messages.getMessage("general.cancelButtonText")) {
          @Override
          public void actionPerformed(ActionEvent e) {
            f.dispose();
            try {
              propertiesConfiguration.save();
            } catch (ConfigurationException e1) {
              e1.printStackTrace();
            }
            System.exit(0);
          }
        });
        contentPane.add(comp);

        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      }
    });
  }

  private static void tryLoadSubstanceLookAndFeel() {
    if (!StringUtils.isNotBlank(System.getProperty("swing.defaultlaf", ""))) {//NON-NLS
      try {
        SubstanceLookAndFeel lookAndFeel = new SubstanceModerateLookAndFeel();
        UIManager.setLookAndFeel(lookAndFeel);
      } catch (UnsupportedLookAndFeelException e) {
        LOGGER.info("Can't change look and feel: ", e);//NON-NLS
      }
    } else {
      LOGGER.info("swing.defaultlaf is set, do not switching to Substance LF");//NON-NLS
    }


  }

  private static byte[] readBytes(InputStream inputStream, long max) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream((int) max);
    byte[] buff = new byte[1024];
    BufferedInputStream bin = new BufferedInputStream(inputStream);
    int read = 0;
    while ((read = bin.read(buff)) > 0 && bout.size() < max) {
      bout.write(buff, 0, read);
    }


    return bout.toByteArray();  //To change body of created methods use File | Settings | File Templates.
  }
}
