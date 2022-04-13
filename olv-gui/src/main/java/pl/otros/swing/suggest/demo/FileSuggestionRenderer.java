/*
 * Copyright 2014 otros.systems@gmail.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package pl.otros.swing.suggest.demo;

import pl.otros.swing.suggest.SuggestionRenderer;

import javax.swing.*;
import java.io.File;
import java.text.DecimalFormat;

/**
 */
class FileSuggestionRenderer implements SuggestionRenderer<File> {

  private final ImageIcon folder;
  private final ImageIcon file;

  public FileSuggestionRenderer() {
    file = new ImageIcon(this.getClass().getResource("/document.png"));
    folder = new ImageIcon(this.getClass().getResource("/folder-open.png"));
  }

  @Override
  public JComponent getSuggestionComponent(File suggestion) {
    Icon i = suggestion.isDirectory() ? folder : file;
    StringBuilder sb = new StringBuilder(suggestion.getAbsolutePath());
    if (suggestion.isFile()) {
      long length = suggestion.length();
      sb.append(" [").append(readableFileSize(length)).append("]");
    }
    return new JLabel(sb.toString(), i, SwingConstants.LEFT);
  }

  public static String readableFileSize(long size) {
    if (size <= 0) return "0";
    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }
}
