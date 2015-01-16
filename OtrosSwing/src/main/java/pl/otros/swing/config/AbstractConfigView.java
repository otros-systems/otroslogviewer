package pl.otros.swing.config;

import javax.swing.*;

public abstract class AbstractConfigView implements ConfigView {
  protected String viewId;
  protected String name;
  protected String description;

  public AbstractConfigView(String viewId, String name, String description) {
    super();
    this.viewId = viewId;
    this.name = name;
    this.description = description;
  }

  @Override
  public String getViewId() {
    return viewId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  protected void addLabel(String string, char c, JComponent jComponent, JPanel panelToAdd) {
    addLabel(string,c,jComponent,jComponent,panelToAdd);
  }
  protected void addLabel(String string, char c, JComponent labelFor,JComponent jComponent, JPanel panelToAdd) {
    JLabel label = new JLabel(string);
    panelToAdd.add(label);
    label.setDisplayedMnemonic(c);
    label.setLabelFor(labelFor);
    panelToAdd.add(jComponent, "growx, wrap");
  }
}
