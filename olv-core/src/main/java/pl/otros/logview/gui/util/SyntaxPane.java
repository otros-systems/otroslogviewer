package pl.otros.logview.gui.util;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.api.theme.ThemeKey;

import javax.swing.*;
import java.awt.*;

public class SyntaxPane {

  public static RSyntaxTextArea propertiesTextArea(Theme theme) {
    final RSyntaxTextArea editorPane = new RSyntaxTextArea();
    editorPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
    SyntaxScheme scheme = editorPane.getSyntaxScheme();
    scheme.getStyle(Token.RESERVED_WORD).foreground = theme.getColor(ThemeKey.LOG_DETAILS_PROPERTY_KEY);
    scheme.getStyle(Token.OPERATOR).foreground = theme.getColor(ThemeKey.LOG_DETAILS_PROPERTY_KEY);
    scheme.getStyle(Token.VARIABLE).foreground = theme.getColor(ThemeKey.LOG_DETAILS_PROPERTY_VALUE);
    scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = theme.getColor(ThemeKey.LOG_DETAILS_PROPERTY_VALUE);
    scheme.getStyle(Token.COMMENT_EOL).foreground = theme.getColor(ThemeKey.LOG_DETAILS_STACKTRACE_COMMENT);
    editorPane.setBackground(new JTextArea().getBackground());

    Color highlightColor;
    if (theme.themeType().equals(Theme.Type.Light)){
      highlightColor = editorPane.getBackground().darker();
    } else {
      highlightColor = editorPane.getBackground().brighter();
    }
    editorPane.setCurrentLineHighlightColor(highlightColor);
    editorPane.revalidate();
    return editorPane;
  }
}
