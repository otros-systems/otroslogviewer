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
import javax.swing.FocusManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SuggestionDocumentListener<T> implements DocumentListener {


  private static final String FOCUS_TO_SUGGESTION = "Focus to suggestion";
  private static final String HIDE_SUGGESTION = "hide to suggestion";
  private final JPanel suggestionPanel;
  private SuggestionRenderer<T> suggestionRenderer;
  private JTextComponent textComponent;
  private SuggestionSource<T> suggestionSource;
  private JWindow suggestionWindow;
  private SelectionListener<T> selectionListener;
  private JComponent[] suggestionComponents;

  private boolean fullyInitialized = false;
  private final ComponentAdapter windowsSizeListener;
  private final FocusAdapter hideSuggestionFocusAdapter;
  private JScrollPane suggestionScrollPane;
  private List<T> lastSuggestions = new ArrayList<>();
  private final KeyStroke toSuggestionKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
  private final KeyStroke hideSuggestionKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
  private Optional<DelayedSwingInvoke> delayedInvoke = Optional.empty();


  @SuppressWarnings("serial")
  public SuggestionDocumentListener(final JTextComponent textComponent, SuggestionSource<T> suggestionSource, SuggestionRenderer<T> suggestionRenderer,
                                    SelectionListener<T> selectionListener) {
    this.textComponent = textComponent;
    this.suggestionSource = suggestionSource;
    this.suggestionRenderer = suggestionRenderer;
    this.selectionListener = selectionListener;

    windowsSizeListener = new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        setSuggestionWindowLocation();
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        setSuggestionWindowLocation();
      }

    };
    suggestionPanel = new JPanel();

    AbstractAction focusToSuggestionAction = new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (suggestionComponents != null && suggestionComponents.length > 0) {
          suggestionComponents[0].requestFocus();
        }
      }
    };
    textComponent.getActionMap().put(FOCUS_TO_SUGGESTION, focusToSuggestionAction);
    textComponent.getActionMap().put(HIDE_SUGGESTION, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        hideSuggestions();
      }
    });

    hideSuggestionFocusAdapter = new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        Component oppositeComponent = e.getOppositeComponent();
        boolean focusOwner = textComponent.isFocusOwner();
        if (!focusOwner && !(oppositeComponent != null && oppositeComponent.getParent() == suggestionPanel)) {
          hideSuggestions();
        }
      }

    };
    textComponent.addFocusListener(hideSuggestionFocusAdapter);
    textComponent.addCaretListener(e -> submitMakeSuggestions());
  }


  private void lazyInit() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(textComponent);
    suggestionWindow = new JWindow(windowAncestor);
    windowAncestor.addComponentListener(windowsSizeListener);
    textComponent.addComponentListener(windowsSizeListener);
    suggestionScrollPane = new JScrollPane(suggestionPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    suggestionWindow.getContentPane().add(suggestionScrollPane);
    suggestionWindow.addFocusListener(hideSuggestionFocusAdapter);
    fullyInitialized = true;

    InputMap inputMap = textComponent.getInputMap();
    suggestionWindow.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        inputMap.remove(toSuggestionKeyStroke);
        inputMap.remove(hideSuggestionKeyStroke);
      }

      @Override
      public void componentShown(ComponentEvent e) {
        inputMap.put(toSuggestionKeyStroke, FOCUS_TO_SUGGESTION);
        inputMap.put(hideSuggestionKeyStroke, HIDE_SUGGESTION);
      }
    });
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    submitMakeSuggestions();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    submitMakeSuggestions();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    submitMakeSuggestions();
  }


  void submitMakeSuggestions(){
    hideSuggestions();
    delayedInvoke.ifPresent(DelayedSwingInvoke::stop);
    final DelayedSwingInvoke delayedSwingInvoke = new DelayedSwingInvoke(400) {

      @Override
      protected void performActionHook() {
        makeSuggestions();
      }
    };
    delayedSwingInvoke.performAction();
    delayedInvoke = Optional.of(delayedSwingInvoke);

  }

  void makeSuggestions() {
    if (SwingUtilities.getWindowAncestor(textComponent) == null) {
      return;
    }
    if (!fullyInitialized) {
      lazyInit();
    }
    final String text = textComponent.getText().replaceAll("\r\n", "\n");
    final int caretPosition = textComponent.getCaretPosition();
    final int selectionEnd = textComponent.getSelectionEnd();
    final int selectionStart = textComponent.getSelectionStart();
    final SuggestionQuery query = new SuggestionQuery(text, caretPosition, selectionStart, selectionEnd);
    List<T> suggestions = suggestionSource.getSuggestions(query);
    int suggestionsSize = suggestions.size();
    final ArrayList<T> diffSuggestions = new ArrayList<>(suggestions);
    lastSuggestions.stream().forEach(diffSuggestions::remove);
    if (diffSuggestions.isEmpty() && suggestions.size() == lastSuggestions.size()) {
      if (suggestionWindow.isVisible()){
        setSuggestionWindowLocation();
        return;
      }
    }
    lastSuggestions = new ArrayList<>();
    if (suggestionsSize == 0) {
      suggestionWindow.setVisible(false);
    } else {
      suggestionPanel.removeAll();
      suggestionPanel.revalidate();
      suggestionPanel.setLayout(new GridLayout(suggestionsSize, 1));
      suggestionComponents = new JComponent[suggestionsSize];
      int index = 0;
      for (final T suggestion : suggestions) {
        final boolean first = index == 0;
        final boolean last = index == suggestionsSize - 1;
        final JComponent suggestionComponent = suggestionRenderer.getSuggestionComponent(suggestion);
        suggestionComponents[index++] = suggestionComponent;
        suggestionComponent.setFocusable(true);
        suggestionComponent.setOpaque(true);
        suggestionComponent.setBorder(BorderFactory.createLineBorder(suggestionPanel.getBackground()));
        suggestionComponent.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            final int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_UP && first) {
              textComponent.requestFocus();
              SuggestDecorator.clearTextFieldSelectionAsync(textComponent);
            } else if (keyCode == KeyEvent.VK_UP) {
              FocusManager.getCurrentManager().focusPreviousComponent();
            } else if (keyCode == KeyEvent.VK_DOWN && !last) {
              FocusManager.getCurrentManager().focusNextComponent();
            } else if (keyCode == KeyEvent.VK_ENTER) {
              suggestionSelected(suggestion, query);
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
              textComponent.requestFocusInWindow();
              hideSuggestions();
            }
          }

          @Override
          public void keyTyped(KeyEvent e) {
            textComponent.dispatchEvent(e);
            textComponent.requestFocus();
          }
        });
        suggestionComponent.addFocusListener(new FocusListener() {
          @Override
          public void focusGained(final FocusEvent e) {
            highlightSuggestion(suggestionComponent);
            SwingUtilities.invokeLater(() -> {
              final Component component = e.getComponent();
              suggestionScrollPane.scrollRectToVisible(component.getBounds());
            });

          }

          @Override
          public void focusLost(FocusEvent e) {
            removeHighlightSuggestion(suggestionComponent);
          }
        });
        suggestionComponent.addMouseListener(new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            suggestionSelected(suggestion, query);
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            highlightSuggestion(suggestionComponent);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            removeHighlightSuggestion(suggestionComponent);
          }

        });
        suggestionPanel.add(suggestionComponent);
      }

      lastSuggestions = suggestions;

      suggestionWindow.pack();
      setSuggestionWindowLocation();
      if (!suggestionWindow.isVisible()) {
        suggestionWindow.setFocusableWindowState(false);
        suggestionWindow.setVisible(true);
        suggestionWindow.setFocusableWindowState(true);
      }
    }
  }


  private void removeHighlightSuggestion(JComponent suggestionComponent) {
    suggestionComponent.setBorder(BorderFactory.createLineBorder(suggestionPanel.getBackground()));
  }


  private void highlightSuggestion(JComponent suggestion) {
    for (JComponent toClearHighlight : suggestionComponents) {
      removeHighlightSuggestion(toClearHighlight);
    }
    suggestion.setBorder(BorderFactory.createLineBorder(suggestion.getForeground()));
    suggestionScrollPane.scrollRectToVisible(suggestion.getBounds());
  }


  protected void hideSuggestions() {
    if (suggestionWindow != null) {
      suggestionWindow.setVisible(false);
    }
  }

  protected void suggestionSelected(T suggestion, SuggestionQuery query) {
    final SuggestionResult<T> result = new SuggestionResult<>(suggestion, query, textComponent);
    selectionListener.selected(result);
    textComponent.requestFocus();
  }

  private void setSuggestionWindowLocation() {
    if (textComponent.isShowing()){
      suggestionWindow.pack();
      if (textComponent instanceof JTextField) {
        int width = Math.max(textComponent.getWidth(), suggestionWindow.getWidth());
        suggestionWindow.setSize(width, (int) Math.min(suggestionWindow.getHeight(), Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2));
        int x = (int) textComponent.getLocationOnScreen().getX();
        int y = (int) (textComponent.getLocationOnScreen().getY() + textComponent.getHeight());
        suggestionWindow.setLocation(x, y);
      } else {
        try {
          final int caretPosition = Math.min(textComponent.getText().length(), textComponent.getCaretPosition());
          final Rectangle rectangle = textComponent.modelToView(caretPosition);
          final Point p = new Point(rectangle.x, rectangle.y + rectangle.height);
          SwingUtilities.convertPointToScreen(p, textComponent);
          suggestionWindow.setLocation(p.x, p.y);
        } catch (BadLocationException e) {
          e.printStackTrace();
        }
      }
    }
  }


  private abstract class DelayedSwingInvoke {

    protected long lastTextFieldEditTime = 0;
    protected int actionDelay = 1000;
    private Optional<Timer> timer = Optional.empty();

    public DelayedSwingInvoke(int actionDelay) {
      this.actionDelay = actionDelay;
    }

    public void performAction() {
      lastTextFieldEditTime = System.currentTimeMillis();
      Timer timer = new Timer(actionDelay, e -> {
        if (System.currentTimeMillis() - lastTextFieldEditTime >= actionDelay) {
          performActionHook();
        }
      });
      timer.setRepeats(false);
      timer.start();
      this.timer = Optional.of(timer);
    }

    public void stop(){
      timer.ifPresent(Timer::stop);
    }

    protected abstract void performActionHook();

  }

}
