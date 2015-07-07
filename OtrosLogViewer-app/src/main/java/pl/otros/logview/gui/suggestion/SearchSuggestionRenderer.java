package pl.otros.logview.gui.suggestion;

import pl.otros.logview.gui.SubText;
import pl.otros.swing.suggest.SuggestionRenderer;

import javax.swing.*;
import java.awt.*;

public class SearchSuggestionRenderer implements SuggestionRenderer<SearchSuggestion> {
  @Override
  public JComponent getSuggestionComponent(SearchSuggestion searchSuggestion) {
    final JLabel label = new JLabel();
    label.setFont(new Font("Courier", Font.PLAIN, label.getFont().getSize()));
    String toDisplay = searchSuggestion.getToDisplay();
    label.setText(toDisplay);
    if (searchSuggestion.getHighlightRange().isPresent()) {
      final SubText intRange = searchSuggestion.getHighlightRange().get();
      final int start = intRange.getStart();
      final int length = intRange.getEnd();
      final StringBuilder sb = new StringBuilder(toDisplay);
      sb.insert(length, "</B></U>");
      sb.insert(start, "<U><B>");
      sb.insert(0, "<HTML>");
      sb.append("</HTML>");
      label.setText(sb.toString());
    }

    return label;
  }
}
