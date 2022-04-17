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

package playground;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class NavBox extends JTextPane {

  private Action actionOnEnter;

  public NavBox() {
    super();
    StyledDocument defaultStyledDocument = this.getStyledDocument();
    StyleContext sc = new StyleContext();
    Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
    Style protocolStyle = sc.addStyle("protocol", defaultStyle);
    StyleConstants.setBold(protocolStyle, true);
    StyleConstants.setForeground(protocolStyle, Color.GREEN);

    Style hostStyle = sc.addStyle("host", defaultStyle);
    StyleConstants.setForeground(hostStyle, Color.BLUE);
    Style fileStyle = sc.addStyle("file", defaultStyle);
    StyleConstants.setForeground(fileStyle, Color.MAGENTA);
    StyleConstants.setBold(fileStyle, true);
    try {
      addText(defaultStyledDocument, protocolStyle, "sftp");
      addText(defaultStyledDocument, defaultStyle, "://");
      addText(defaultStyledDocument, hostStyle, "my.server.com");
      addText(defaultStyledDocument, defaultStyle, "/opt/app/logs/");
      addText(defaultStyledDocument, fileStyle, "some.log");
    } catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    this.addKeyListener(new KeyAdapter() {


      @Override
      public void keyPressed(KeyEvent e) {
        if ('\n' == e.getKeyChar()) {
          e.consume();
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if ('\n' == e.getKeyChar()) {
          e.consume();
        }
      }

      @Override
      public void keyTyped(KeyEvent e) {
        if ('\n' == e.getKeyChar()) {
          e.consume();
        }
      }
    });
  }

  private void addText(StyledDocument doc, Style style, String text) throws BadLocationException {
    doc.insertString(doc.getLength(), text, style);
  }

  public void setActionOnEnter(Action actionOnEnter) {
    this.actionOnEnter = actionOnEnter;

  }

  public static void main(String[] args) {
    JFrame f = new JFrame("a");
    JToolBar bar = new JToolBar();

    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().setLayout(new FlowLayout());
    System.out.println(f.getContentPane().getLayout().getClass().getName());
    NavBox navBox = new NavBox();
    navBox.setSize(500, 40);
    navBox.setMaximumSize(new Dimension(500, 20));
    navBox.setMinimumSize(new Dimension(500, 20));
    f.getContentPane().add(bar);
    bar.add(navBox);
    bar.add(new JButton("G"));
    f.setSize(300, 60);
    f.setVisible(true);
  }

}
