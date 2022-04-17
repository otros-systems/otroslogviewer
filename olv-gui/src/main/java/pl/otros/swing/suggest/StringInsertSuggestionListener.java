package pl.otros.swing.suggest;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class StringInsertSuggestionListener implements SelectionListener<BasicSuggestion> {
  @Override
  public void selected(SuggestionResult<BasicSuggestion> result) {
    final Document document = result.getTextComponent().getDocument();
    try {
      final int caretLocation = result.getSuggestionSource().getCaretLocation();
      final String toInsert = result.getValue().getToInsert();
      document.insertString(caretLocation, toInsert, null);
    } catch (BadLocationException e) {
      //TODO
      e.printStackTrace();
    }
  }
}
