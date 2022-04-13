package pl.otros.swing.rulerbar;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class OtrosJTextWithRulerScrollPane<T extends JTextComponent> extends JPanel {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final T jTextComponent;
  private final JScrollPane jScrollPane;
  private final RulerBar rulerBar;
  private final DefaultMarkerModel markerModel;

  public OtrosJTextWithRulerScrollPane(T jTextComponent) {
    super(new BorderLayout());
    this.jTextComponent = jTextComponent;
    markerModel = new DefaultMarkerModel();
    rulerBar = new RulerBar(markerModel);
    jScrollPane = new JScrollPane(jTextComponent);
    this.add(jScrollPane);
    this.add(rulerBar, BorderLayout.EAST);
  }

  public T getjTextComponent() {
    return jTextComponent;
  }

  public JScrollPane getjScrollPane() {
    return jScrollPane;
  }

  protected RulerBar getRulerBar() {
    return rulerBar;
  }

  protected DefaultMarkerModel getMarkerModel() {
    return markerModel;
  }


}
