package pl.otros.swing.config;

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
  public String getDescirption() {
    return description;
  }
}
