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

package pl.otros.vfs.browser.preview;


import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import pl.otros.vfs.browser.i18n.Messages;
import pl.otros.vfs.browser.preview.PreviewStatus.State;

public class PreviewComponent extends JPanel {

  private static final String EMPTY_STRING = "";
  private JLabel titleLabel;
  private JLabel nameLabel;
  private JTextArea contentArea;
  private JProgressBar progressBar;
  private JCheckBox enabledCheckBox;
  private PreviewStatus previewStatus;
  private JScrollPane contentScrollPane;
  private final TitledBorder border;

  public PreviewComponent() {
    super(new MigLayout());
    titleLabel = new JLabel(Messages.getMessage("preview.label"));
    nameLabel = new JLabel();
    contentArea = new JTextArea();
    contentArea.setEditable(false);
    border = BorderFactory.createTitledBorder(Messages.getMessage("preview.fileContent"));
    contentArea.setBorder(border);
    contentArea.setAutoscrolls(false);
    contentArea.setFont(new Font("Courier New", Font.PLAIN, contentArea.getFont().getSize()));

    contentScrollPane = new JScrollPane(contentArea);
    contentScrollPane.setAutoscrolls(false);

    progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setMaximum(1);
    enabledCheckBox = new JCheckBox(Messages.getMessage("preview.enable"), true);
    enabledCheckBox.setMnemonic(Messages.getMessage("preview.enable.mnemonic").charAt(0));
    enabledCheckBox.setRolloverEnabled(true);
    enabledCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        boolean enabled = enabledCheckBox.isSelected();
        progressBar.setEnabled(enabled);
        contentScrollPane.setEnabled(enabled);
        contentArea.setEnabled(enabled);
        nameLabel.setEnabled(enabled);
        titleLabel.setEnabled(enabled);
      }
    });

    add(titleLabel, "dock north, gap 5 5 5 5, center");
    add(nameLabel, "dock north, gap 5 5 5 5");
    add(contentScrollPane, "dock center, gap 5 5 5 5");
    add(enabledCheckBox, "dock south, gap 0 5 5 5");
    add(progressBar, "dock south, gap 5 5 5 5");
  }

  public void setPreviewStatus(PreviewStatus previewStatus) {
    this.previewStatus = previewStatus;
    updateUi(previewStatus);
  }


  private void updateUi(PreviewStatus previewStatus) {
    nameLabel.setText(previewStatus.getName());
    if (State.NA.equals(previewStatus.getState())) {
      progressBar.setIndeterminate(false);
      progressBar.setString(EMPTY_STRING);
      progressBar.setValue(0);
      contentArea.setText(EMPTY_STRING);
      nameLabel.setText(Messages.getMessage("preview.n/a"));
    } else if (State.NOT_STARTED.equals(previewStatus.getState())) {
      progressBar.setIndeterminate(true);
      progressBar.setString(Messages.getMessage("browser.loading..."));
      contentArea.setText(EMPTY_STRING);
    } else if (State.LOADING.equals(previewStatus.getState())) {
      progressBar.setIndeterminate(false);
      progressBar.setMaximum(previewStatus.getMaxToLoad());
      progressBar.setValue(previewStatus.getLoaded());
      progressBar.setString(Messages.getMessage("preview.loadedXOf", previewStatus.getLoaded(), previewStatus.getMaxToLoad(), previewStatus.getLoadUnit()));
      contentArea.setText(new String(previewStatus.getContent()));
    } else if (State.FINISHED.equals(previewStatus.getState())) {
      progressBar.setIndeterminate(false);
      progressBar.setValue(progressBar.getMaximum());
      progressBar.setString(Messages.getMessage("preview.loadedX", previewStatus.getLoaded(), previewStatus.getLoadUnit()));
      contentArea.setText(tryToUngzip(previewStatus.getContent()));
      contentArea.setCaretPosition(0);
    } else if (State.CANCELLED.equals(previewStatus.getState())) {
      //Do not change, another refresh will change this
    } else if (State.ERROR.equals(previewStatus.getState())) {
      progressBar.setIndeterminate(false);
      progressBar.setMaximum(previewStatus.getMaxToLoad());
      progressBar.setValue(previewStatus.getLoaded());
      progressBar.setString(Messages.getMessage("preview.errorLoadingFile"));
      contentArea.setText(tryToUngzip(previewStatus.getContent()));
      contentArea.setCaretPosition(0);
    }

  }

  public boolean isPreviewEnabled() {
    return enabledCheckBox.isSelected();
  }

  private String tryToUngzip(byte[] bytes){	  
	  int bytesLength = bytes.length;
	  try {
		  GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(bytes));
		  try {
			  bytesLength = gzis.read(bytes,0,bytes.length);
		  } catch (IOException e){
			  //can't read
		  }
		  
	  } catch (IOException e) {
      //ignore this
	  }
	  return new String(bytes,0,bytesLength);
  }
  
}
