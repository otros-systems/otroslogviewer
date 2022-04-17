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

package pl.otros.swing.suggest;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SuggestDecorator {

  /**
   * Add popup with suggestion to text component
   * @param textComponent  text component
   * @param suggestionSource source of suggestions
   * @param suggestionRenderer renderer for suggestions
   * @param selectionListener suggestion listener to be executed after suggestion is selected
   * @param <T> Suggestion type
   */
  public static <T> void decorate(final JTextComponent textComponent,
                                  SuggestionSource<T> suggestionSource,
                                  SuggestionRenderer<T> suggestionRenderer,
                                  SelectionListener<T> selectionListener) {
    decorate(textComponent, suggestionSource, suggestionRenderer, selectionListener, false);
  }


  /**
   * Add popup with suggestion to text component
   * @param textComponent  text component
   * @param suggestionSource source of suggestions
   * @param suggestionRenderer renderer for suggestions
   * @param selectionListener suggestion listener to be executed after suggestion is selected
   * @param clearFocusAfterSelection true if text selection should be removed and caret set to end of text after selecting suggestion
   * @param <T> Suggestion type
   */
  public static <T> void decorate(final JTextComponent textComponent,
                                  SuggestionSource<T> suggestionSource,
                                  SuggestionRenderer<T> suggestionRenderer,
                                  SelectionListener<T> selectionListener,
                                  boolean clearFocusAfterSelection) {

    Document document = textComponent.getDocument();
    SuggestionDocumentListener<? extends T> listener = new SuggestionDocumentListener<>(textComponent, suggestionSource, suggestionRenderer, selectionListener);
    document.addDocumentListener(listener);
    if (clearFocusAfterSelection) {
      textComponent.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          //do not select all on OSX after suggestion is selected
          if (e.getOppositeComponent() == null) {
            clearTextFieldSelectionAsync(textComponent);
          }
        }
      });
    }
  }

  static void clearTextFieldSelectionAsync(final JTextComponent textField) {
    SwingUtilities.invokeLater(() -> {
      textField.select(0, 0);
      final int length = textField.getDocument().getLength();
      try {
        textField.setCaretPosition(length);
      } catch (Exception ignore) {
        System.err.println("Can't set caret position to length: " + length);
      }
    });
  }
}
