package pl.otros.swing.table;

import java.util.List;

public class ColumnLayout {

  private final String name;
  private final List<String> columns;

  public ColumnLayout(String name, List<String> columns) {
    this.name = name;
    this.columns = columns;
  }

  public List<String> getColumns() {
    return columns;
  }

  public String getName() {
    return name;
  }
}
