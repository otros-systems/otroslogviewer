package pl.otros.swing.table;

import java.util.List;

public class ColumnLayout {

  private String name;
  private List<String> columns;

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
